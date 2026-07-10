package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.enums.PosOnbordingExclusionRejectionReason;
import it.gov.pagopa.merchant.dto.enums.PosOnbordingRejectionReason;
import it.gov.pagopa.merchant.dto.pointofsales.*;
import it.gov.pagopa.merchant.exception.custom.InitiativeNotValidException;
import it.gov.pagopa.merchant.exception.custom.MerchantNotAllowedException;
import it.gov.pagopa.merchant.exception.custom.PosValidationException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.repository.PointOfSalesInitiativeRepository;
import it.gov.pagopa.merchant.service.KeycloakService;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointOfSaleWriterImpl implements PointOfSaleWriter {

  private final MerchantService merchantService;
  private final PointOfSaleRepository pointOfSaleRepository;
  private final KeycloakService keycloakService;
  private final PointOfSaleDTOMapper mapper;
  private final PointOfSalesInitiativeRepository pointOfSalesInitiativeRepository;
  private final TransactionConnector transactionConnector;

  @Override
  public void savePointOfSales(String merchantId, String initiativeId, List<PointOfSaleDTO> dtos) {

    Merchant merchant = merchantService.getMerchantByMerchantId(merchantId);

    Initiative initiative = merchant.getInitiativeList().stream()
            .filter(i -> initiativeId.equals(i.getInitiativeId()))
            .findFirst()
            .orElseThrow(() -> new MerchantNotAllowedException(
                    String.format(
                            "Merchant with id %s not onboarded on initiative %s",
                            merchantId,
                            initiativeId
                    )
            ));

    if (initiative.getEndDate().isBefore(LocalDate.now())) {
      throw new InitiativeNotValidException(
              String.format("Initiative %s ended", initiativeId)
      );
    }

    List<PointOfSale> entities = dtos.stream()
            .map(dto -> mapper.dtoToEntity(dto, merchantId))
            .toList();

    List<ValidationErrorDetail> errors =
            validate(entities, dtos, merchantId, initiativeId);

    if (!errors.isEmpty()) {
      throw new PosValidationException(errors);
    }

    Set<PointOfSale> saved = new HashSet<>();
    Set<PointOfSalesInitiative> associations = new HashSet<>();

    try {

      for (int i = 0; i < entities.size(); i++) {

        PointOfSale entity = entities.get(i);
        PointOfSaleDTO dto = dtos.get(i);

        PointOfSale persisted = pointOfSaleRepository.save(entity);

        keycloakService.manageReferentUserOnKeycloak(
                persisted,
                dto.getContactEmail()
        );

        saved.add(persisted);

        associations.add(
                buildAssociation(
                        persisted.getId(),
                        merchantId,
                        initiativeId
                )
        );
      }

      pointOfSalesInitiativeRepository.saveAll(associations);

    } catch (Exception ex) {
      compensate(saved, merchantId, initiativeId);
      throw handle(ex);
    }
  }

  private List<ValidationErrorDetail> validate(
          List<PointOfSale> entities,
          List<PointOfSaleDTO> dtos,
          String merchantId,
          String initiativeId) {

    List<ValidationErrorDetail> errors = new ArrayList<>();

    for (int i = 0; i < entities.size(); i++) {

      PointOfSale entity = entities.get(i);
      PointOfSaleDTO dto = dtos.get(i);
      int index = i;

      boolean emailExists = pointOfSaleRepository
              .findByContactEmail(dto.getContactEmail())
              .map(e -> {
                errors.add(emailError(index, dto.getContactEmail()));
                return true;
              })
              .orElse(false);

      if (emailExists) {
        continue;
      }

      pointOfSaleRepository.findDuplicate(entity).ifPresent(existing -> {

        boolean sameInitiative = pointOfSalesInitiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                        existing.getId(), initiativeId, merchantId)
                .isPresent();

        errors.add(posError(index, dto, existing, sameInitiative));
      });
    }

    return errors;
  }

  private ValidationErrorDetail emailError(int index, String email) {
    return ValidationErrorDetail.builder()
            .index(index)
            .field("contactEmail")
            .value(email)
            .code(PointOfSaleConstants.CODE_EMAIL_ALREADY_REGISTERED)
            .message("Email already registered")
            .build();
  }

  private ValidationErrorDetail posError(int index,
                                         PointOfSaleDTO dto,
                                         PointOfSale existing,
                                         boolean sameInitiative) {

    if (sameInitiative) {
      return ValidationErrorDetail.builder()
              .index(index)
              .field(dto.getType() == PointOfSaleTypeEnum.ONLINE ? "website" : "address")
              .value(dto.getType() == PointOfSaleTypeEnum.ONLINE ? dto.getWebsite() : dto.getAddress())
              .code(dto.getType() == PointOfSaleTypeEnum.ONLINE
                      ? PointOfSaleConstants.CODE_ONLINE_POS_ALREADY_REGISTERED
                      : PointOfSaleConstants.CODE_PHYSICAL_POS_ALREADY_REGISTERED)
              .message("POS already registered on this initiative")
              .build();
    }

    return ValidationErrorDetail.builder()
            .index(index)
            .field("pos")
            .value(existing.getId())
            .code(PointOfSaleConstants.CODE_POS_ALREADY_REGISTERED_OTHER_INITIATIVE)
            .message("POS already registered on another initiative")
            .build();
  }

  private void compensate(Set<PointOfSale> saved, String merchantId, String initiativeId) {

    try {
      List<String> ids = saved.stream()
              .map(PointOfSale::getId)
              .toList();

      pointOfSalesInitiativeRepository
              .deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(
                      merchantId, initiativeId, ids
              );

      for (PointOfSale pos : saved) {
        removeEntity(pos);
      }

    } catch (Exception ex) {
      log.error("[COMPENSATION] global failure", ex);
    }
  }

  private void removeEntity(PointOfSale pos) {
    try {
      pointOfSaleRepository.deleteById(pos.getId());
      removeKeycloakUser(pos.getContactEmail());
    } catch (Exception ex) {
      log.error("[COMPENSATION] failed for pos {}", sanitizeString(pos.getId()), ex);
    }
  }

  private void removeKeycloakUser(String email) {

    try {
      UsersResource usersResource = keycloakService.getUserResource();

      List<UserRepresentation> users =
              usersResource.searchByEmail(email, true);

      for (UserRepresentation user : users) {
        usersResource.get(user.getId()).remove();
      }

    } catch (Exception ex) {
      log.error("[COMPENSATION][KEYCLOAK] failed for email {}", sanitizeString(email), ex);
    }
  }

  private RuntimeException handle(Exception ex) {

    if (ex instanceof DuplicateKeyException) {
      return new ServiceException(
              PointOfSaleConstants.CODE_ALREADY_REGISTERED,
              "duplicate"
      );
    }

    if (ex instanceof IncorrectResultSizeDataAccessException) {
      return new ServiceException(
              PointOfSaleConstants.CODE_DATA_INCONSISTENCY,
              "inconsistent DB"
      );
    }

    return new ServiceException(
            PointOfSaleConstants.CODE_GENERIC_SAVE_ERROR,
            PointOfSaleConstants.MSG_GENERIC_SAVE_ERROR
    );
  }

  private PointOfSalesInitiative buildAssociation(
          String posId,
          String merchantId,
          String initiativeId) {

    Instant now = Instant.now();

    return PointOfSalesInitiative.builder()
            .pointOfSaleId(posId)
            .merchantId(merchantId)
            .initiativeId(initiativeId)
            .enabled(true)
            .createdAt(now)
            .updatedAt(now)
            .build();
  }

  @Override
  public PointOfSaleOnboardingResultDTO onboardingPointOfSales(
          String merchantId,
          String initiativeId,
          List<String> pointOfSaleIds) {

    Merchant merchant = merchantService.getMerchantByMerchantId(merchantId);

    Initiative initiative = merchant.getInitiativeList().stream()
            .filter(i -> initiativeId.equals(i.getInitiativeId()))
            .findFirst()
            .orElseThrow(() -> new MerchantNotAllowedException(
                    String.format(
                            "Merchant with id %s not onboarded on initiative %s",
                            merchantId,
                            initiativeId
                    )
            ));

    if (initiative.getEndDate().isBefore(LocalDate.now())) {
      throw new InitiativeNotValidException(
              String.format("Initiative %s ended", initiativeId)
      );
    }

    List<AssociatedPointOfSaleDTO> associated = new ArrayList<>();
    List<NotAssociatedPointOfSaleDTO> notAssociated = new ArrayList<>();


    for (String posId : pointOfSaleIds) {

      try {

        AssociatedPointOfSaleDTO associatedEntry = null;
        NotAssociatedPointOfSaleDTO notAssociatedEntry = null;

        Optional<PointOfSale> posOpt = pointOfSaleRepository.findById(posId);

        if (posOpt.isEmpty()) {

          notAssociatedEntry = NotAssociatedPointOfSaleDTO.builder()
                  .pointOfSaleId(posId)
                  .reason(PosOnbordingRejectionReason.NOT_FOUND)
                  .build();

        } else {

          PointOfSale pos = posOpt.get();

          if (!merchantId.equals(pos.getMerchantId())) {

            notAssociatedEntry = NotAssociatedPointOfSaleDTO.builder()
                    .pointOfSaleId(posId)
                    .pointOfSaleName(pos.getFranchiseName())
                    .reason(PosOnbordingRejectionReason.INVALID)
                    .address(posOpt.get().getAddress())
                    .city(posOpt.get().getCity())
                    .streetNumber(posOpt.get().getStreetNumber())
                    .website(posOpt.get().getWebsite())
                    .type(PointOfSaleTypeEnum.valueOf(posOpt.get().getType()))
                    .build();

          } else if (pointOfSalesInitiativeRepository
                  .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                          posId, initiativeId, merchantId)
                  .isPresent()) {

            notAssociatedEntry = NotAssociatedPointOfSaleDTO.builder()
                    .pointOfSaleId(posId)
                    .pointOfSaleName(pos.getFranchiseName())
                    .reason(PosOnbordingRejectionReason.ALREADY_ASSOCIATED)
                    .address(posOpt.get().getAddress())
                    .city(posOpt.get().getCity())
                    .streetNumber(posOpt.get().getStreetNumber())
                    .website(posOpt.get().getWebsite())
                    .type(PointOfSaleTypeEnum.valueOf(posOpt.get().getType()))
                    .build();

          } else {

            pointOfSalesInitiativeRepository.save(
                    PointOfSalesInitiative.builder()
                            .pointOfSaleId(posId)
                            .initiativeId(initiativeId)
                            .merchantId(merchantId)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .enabled(true)
                            .build()
            );

            associatedEntry = AssociatedPointOfSaleDTO.builder()
                    .pointOfSaleId(posId)
                    .pointOfSaleName(pos.getFranchiseName())
                    .build();
          }
        }

        if (associatedEntry != null) {
          associated.add(associatedEntry);
        } else {
          notAssociated.add(notAssociatedEntry);
        }

      } catch (Exception ex) {
        notAssociated.add(NotAssociatedPointOfSaleDTO.builder()
                .pointOfSaleId(posId)
                .reason(PosOnbordingRejectionReason.GENERIC_ERROR)
                .build());
      }
    }


    PointOfSaleOnboardingResultDTO result = new PointOfSaleOnboardingResultDTO();
    result.setAssociated(associated);
    result.setNotAssociated(notAssociated);

    return result;
  }

  @Override
  public PointOfSaleExclusionResultDTO excludePointsOfSales(String merchantId, String initiativeId, List<String> pointOfSaleIds) {
    log.info("[POINT-OF-SALE][EXCLUSION] Processing rich partial exclusion for merchantId={} on initiativeId={}",
            merchantId, initiativeId);

    Merchant merchant = merchantService.getMerchantByMerchantId(merchantId);
    Initiative initiative = merchant.getInitiativeList().stream()
            .filter(i -> initiativeId.equals(i.getInitiativeId()))
            .findFirst()
            .orElseThrow(() -> new MerchantNotAllowedException(
                    String.format("Merchant with id %s not onboarded on initiative %s", merchantId, initiativeId)
            ));

    if (initiative.getEndDate().isBefore(LocalDate.now())) {
      throw new InitiativeNotValidException(String.format("Initiative %s ended", initiativeId));
    }

    MerchantTransactionsListDTO transactions =
            transactionConnector.getMerchantTransactions(merchantId, initiativeId, null, null, null);

    List<ExcludedPointOfSaleDetailDTO> excluded = new ArrayList<>();
    List<NotExcludedPointOfSaleDTO> notExcluded = new ArrayList<>();

    for (String posId : pointOfSaleIds) {
      try {
        Optional<PointOfSale> posOpt = pointOfSaleRepository.findById(posId);

        if (posOpt.isEmpty()) {
          notExcluded.add(NotExcludedPointOfSaleDTO.builder()
                  .pointOfSaleId(posId)
                  .reason(PosOnbordingExclusionRejectionReason.NOT_FOUND)
                  .build());
        } else {
          PointOfSale pos = posOpt.get();

          Optional<PointOfSalesInitiative> associationOpt = pointOfSalesInitiativeRepository
                  .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(posId, initiativeId, merchantId);

          if (associationOpt.isEmpty()) {
            log.info("[POINT-OF-SALE][EXCLUSION] POS {} skipped: already excluded", sanitizeString(posId));
            notExcluded.add(NotExcludedPointOfSaleDTO.builder()
                    .pointOfSaleId(posId)
                    .reason(PosOnbordingExclusionRejectionReason.ALREADY_EXCLUDED)
                    .build());

          } else if (hasBlockingTransactions(transactions, posId)) {
            log.info("[POINT-OF-SALE][EXCLUSION] POS {} skipped: has active transactions", sanitizeString(posId));
            notExcluded.add(NotExcludedPointOfSaleDTO.builder()
                    .pointOfSaleId(posId)
                    .reason(PosOnbordingExclusionRejectionReason.HAS_TRANSACTIONS)
                    .build());

          } else {
            PointOfSalesInitiative association = associationOpt.get();
            association.setEnabled(false);
            association.setUpdatedAt(Instant.now());
            pointOfSalesInitiativeRepository.save(association);

            excluded.add(ExcludedPointOfSaleDetailDTO.builder()
                    .pointOfSaleId(posId)
                    .franchiseName(pos.getFranchiseName())
                    .build());

            log.info("[POINT-OF-SALE][EXCLUSION] POS {} successfully excluded", sanitizeString(posId));
          }
        }

      } catch (Exception ex) {
        log.error("[POINT-OF-SALE][EXCLUSION] Generic error processing POS {}", sanitizeString(posId), ex);
        notExcluded.add(NotExcludedPointOfSaleDTO.builder()
                .pointOfSaleId(posId)
                .reason(PosOnbordingExclusionRejectionReason.GENERIC_ERROR)
                .build());
      }
    }

    return PointOfSaleExclusionResultDTO.builder()
            .excludedPointOfSales(excluded)
            .notExcludedPointOfSales(notExcluded)
            .build();
  }

  private boolean hasBlockingTransactions(MerchantTransactionsListDTO transactions, String posId) {
    if (transactions == null || transactions.getContent() == null) {
      return false;
    }

    return transactions.getContent().stream()
            .anyMatch(t -> posId.equals(t.getPointOfSaleId()));
  }
}
