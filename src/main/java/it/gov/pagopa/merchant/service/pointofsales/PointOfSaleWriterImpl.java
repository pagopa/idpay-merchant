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

import static it.gov.pagopa.common.utils.CommonConstants.ZONEID;
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

  private static final String ERROR_MERCHANT_NOT_ONBOARDED = "Merchant with id %s not onboarded on initiative %s";
  private static final String ERROR_INITIATIVE_ENDED = "Initiative %s ended";

  @Override
  public void savePointOfSales(String merchantId, String initiativeId, List<PointOfSaleDTO> dtos) {
    validateMerchantInitiative(merchantId, initiativeId, LocalDate.now());

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
    validateMerchantInitiative(merchantId, initiativeId, LocalDate.now());

    List<AssociatedPointOfSaleDTO> associated = new ArrayList<>();
    List<NotAssociatedPointOfSaleDTO> notAssociated = new ArrayList<>();

    for (String posId : pointOfSaleIds) {
      processSingleOnboarding(merchantId, initiativeId, posId, associated, notAssociated);
    }

    return buildOnboardingResult(associated, notAssociated);
  }

  private void processSingleOnboarding(
          String merchantId,
          String initiativeId,
          String posId,
          List<AssociatedPointOfSaleDTO> associated,
          List<NotAssociatedPointOfSaleDTO> notAssociated) {

    try {
      Optional<PointOfSale> posOpt = pointOfSaleRepository.findById(posId);

      if (posOpt.isEmpty()) {
        notAssociated.add(buildNotFoundOnboardingEntry(posId));
        return;
      }

      PointOfSale pos = posOpt.get();

      if (!merchantId.equals(pos.getMerchantId())) {
        notAssociated.add(buildInvalidOnboardingEntry(pos));
        return;
      }

      Optional<PointOfSalesInitiative> existingAssociationOpt =
              pointOfSalesInitiativeRepository.findByPointOfSaleIdAndInitiativeIdAndMerchantId(
                      posId,
                      initiativeId,
                      merchantId
              );

      if (isAlreadyAssociated(existingAssociationOpt)) {
        notAssociated.add(buildAlreadyAssociatedOnboardingEntry(pos));
        return;
      }

      upsertAssociation(existingAssociationOpt, posId, merchantId, initiativeId);
      associated.add(buildAssociatedOnboardingEntry(pos));
    } catch (Exception _) {
      notAssociated.add(buildGenericErrorOnboardingEntry(posId));
    }
  }

  private PointOfSaleOnboardingResultDTO buildOnboardingResult(
          List<AssociatedPointOfSaleDTO> associated,
          List<NotAssociatedPointOfSaleDTO> notAssociated) {

    PointOfSaleOnboardingResultDTO result = new PointOfSaleOnboardingResultDTO();
    result.setAssociated(associated);
    result.setNotAssociated(notAssociated);
    return result;
  }

  private NotAssociatedPointOfSaleDTO buildNotFoundOnboardingEntry(String posId) {
    return NotAssociatedPointOfSaleDTO.builder()
            .pointOfSaleId(posId)
            .reason(PosOnbordingRejectionReason.NOT_FOUND)
            .build();
  }

  private NotAssociatedPointOfSaleDTO buildInvalidOnboardingEntry(PointOfSale pos) {
    return NotAssociatedPointOfSaleDTO.builder()
            .pointOfSaleId(pos.getId())
            .franchiseName(pos.getFranchiseName())
            .reason(PosOnbordingRejectionReason.INVALID)
            .address(pos.getAddress())
            .city(pos.getCity())
            .streetNumber(pos.getStreetNumber())
            .website(pos.getWebsite())
            .type(PointOfSaleTypeEnum.valueOf(pos.getType()))
            .build();
  }

  private NotAssociatedPointOfSaleDTO buildAlreadyAssociatedOnboardingEntry(PointOfSale pos) {
    return NotAssociatedPointOfSaleDTO.builder()
            .pointOfSaleId(pos.getId())
            .franchiseName(pos.getFranchiseName())
            .reason(PosOnbordingRejectionReason.ALREADY_ASSOCIATED)
            .address(pos.getAddress())
            .city(pos.getCity())
            .streetNumber(pos.getStreetNumber())
            .website(pos.getWebsite())
            .type(PointOfSaleTypeEnum.valueOf(pos.getType()))
            .build();
  }

  private NotAssociatedPointOfSaleDTO buildGenericErrorOnboardingEntry(String posId) {
    return NotAssociatedPointOfSaleDTO.builder()
            .pointOfSaleId(posId)
            .reason(PosOnbordingRejectionReason.GENERIC_ERROR)
            .build();
  }

  private AssociatedPointOfSaleDTO buildAssociatedOnboardingEntry(PointOfSale pos) {
    return AssociatedPointOfSaleDTO.builder()
            .pointOfSaleId(pos.getId())
            .franchiseName(pos.getFranchiseName())
            .build();
  }

  private boolean isAlreadyAssociated(Optional<PointOfSalesInitiative> existingAssociationOpt) {
    return existingAssociationOpt.isPresent() && Boolean.TRUE.equals(existingAssociationOpt.get().getEnabled());
  }

  private void upsertAssociation(
          Optional<PointOfSalesInitiative> existingAssociationOpt,
          String posId,
          String merchantId,
          String initiativeId) {

    if (existingAssociationOpt.isPresent()) {
      PointOfSalesInitiative existing = existingAssociationOpt.get();
      existing.setEnabled(true);
      existing.setUpdatedAt(Instant.now());
      pointOfSalesInitiativeRepository.save(existing);
      return;
    }

    pointOfSalesInitiativeRepository.save(buildAssociation(posId, merchantId, initiativeId));
  }

  @Override
  public PointOfSaleExclusionResultDTO excludePointsOfSales(String merchantId, String initiativeId, List<String> pointOfSaleIds) {
    log.info("[POINT-OF-SALE][EXCLUSION] Processing rich partial exclusion for merchantId={} on initiativeId={}",
            merchantId, initiativeId);

    validateMerchantInitiative(merchantId, initiativeId, LocalDate.now(ZONEID));

    MerchantTransactionsListDTO transactions =
            transactionConnector.getMerchantTransactions(merchantId, initiativeId, null, null, null);

    List<ExcludedPointOfSaleDetailDTO> excluded = new ArrayList<>();
    List<NotExcludedPointOfSaleDTO> notExcluded = new ArrayList<>();

    for (String posId : pointOfSaleIds) {
      processSingleExclusion(merchantId, initiativeId, transactions, posId, excluded, notExcluded);
    }

    return PointOfSaleExclusionResultDTO.builder()
            .excludedPointOfSales(excluded)
            .notExcludedPointOfSales(notExcluded)
            .build();
  }

  private void validateMerchantInitiative(String merchantId, String initiativeId, LocalDate currentDate) {
    Merchant merchant = merchantService.getMerchantByMerchantId(merchantId);

    Initiative initiative = merchant.getInitiativeList().stream()
            .filter(i -> initiativeId.equals(i.getInitiativeId()))
            .findFirst()
            .orElseThrow(() -> new MerchantNotAllowedException(
                    String.format(ERROR_MERCHANT_NOT_ONBOARDED, merchantId, initiativeId)
            ));

    if (initiative.getEndDate().isBefore(currentDate)) {
      throw new InitiativeNotValidException(String.format(ERROR_INITIATIVE_ENDED, initiativeId));
    }

  }

  private void processSingleExclusion(
          String merchantId,
          String initiativeId,
          MerchantTransactionsListDTO transactions,
          String posId,
          List<ExcludedPointOfSaleDetailDTO> excluded,
          List<NotExcludedPointOfSaleDTO> notExcluded) {

    try {
      Optional<PointOfSale> posOpt = pointOfSaleRepository.findById(posId);
      if (posOpt.isEmpty()) {
        notExcluded.add(buildNotFoundExclusionEntry(posId));
        return;
      }

      PointOfSale pos = posOpt.get();
      Optional<PointOfSalesInitiative> associationOpt = pointOfSalesInitiativeRepository
              .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(posId, initiativeId, merchantId);

      if (associationOpt.isEmpty()) {
        log.info("[POINT-OF-SALE][EXCLUSION] POS {} skipped: already excluded", sanitizeString(posId));
        notExcluded.add(buildNotExcludedExclusionEntry(pos, PosOnbordingExclusionRejectionReason.ALREADY_EXCLUDED));
        return;
      }

      if (hasBlockingTransactions(transactions, posId)) {
        log.info("[POINT-OF-SALE][EXCLUSION] POS {} skipped: has active transactions", sanitizeString(posId));
        notExcluded.add(buildNotExcludedExclusionEntry(pos, PosOnbordingExclusionRejectionReason.HAS_TRANSACTIONS));
        return;
      }

      disableAssociation(associationOpt.get());
      excluded.add(buildExcludedDetail(pos));
      log.info("[POINT-OF-SALE][EXCLUSION] POS {} successfully excluded", sanitizeString(posId));
    } catch (Exception ex) {
      log.error("[POINT-OF-SALE][EXCLUSION] Generic error processing POS {}", sanitizeString(posId), ex);
      notExcluded.add(buildGenericErrorExclusionEntry(posId));
    }
  }

  private NotExcludedPointOfSaleDTO buildNotFoundExclusionEntry(String posId) {
    return NotExcludedPointOfSaleDTO.builder()
            .pointOfSaleId(posId)
            .reason(PosOnbordingExclusionRejectionReason.NOT_FOUND)
            .build();
  }

  private NotExcludedPointOfSaleDTO buildNotExcludedExclusionEntry(
          PointOfSale pos,
          PosOnbordingExclusionRejectionReason reason) {

    return NotExcludedPointOfSaleDTO.builder()
            .pointOfSaleId(pos.getId())
            .franchiseName(pos.getFranchiseName())
            .type(pos.getType())
            .address(pos.getAddress())
            .streetNumber(pos.getStreetNumber())
            .city(pos.getCity())
            .website(pos.getWebsite())
            .reason(reason)
            .build();
  }

  private NotExcludedPointOfSaleDTO buildGenericErrorExclusionEntry(String posId) {
    return NotExcludedPointOfSaleDTO.builder()
            .pointOfSaleId(posId)
            .reason(PosOnbordingExclusionRejectionReason.GENERIC_ERROR)
            .build();
  }

  private ExcludedPointOfSaleDetailDTO buildExcludedDetail(PointOfSale pos) {
    return ExcludedPointOfSaleDetailDTO.builder()
            .pointOfSaleId(pos.getId())
            .franchiseName(pos.getFranchiseName())
            .build();
  }

  private void disableAssociation(PointOfSalesInitiative association) {
    association.setEnabled(false);
    association.setUpdatedAt(Instant.now());
    pointOfSalesInitiativeRepository.save(association);
  }

  private boolean hasBlockingTransactions(MerchantTransactionsListDTO transactions, String posId) {
    if (transactions == null || transactions.getContent() == null) {
      return false;
    }

    return transactions.getContent().stream()
            .anyMatch(t -> posId.equals(t.getPointOfSaleId()));
  }
}
