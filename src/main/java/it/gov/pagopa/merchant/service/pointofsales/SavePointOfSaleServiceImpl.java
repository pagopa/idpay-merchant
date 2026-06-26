package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.exception.custom.PosValidationException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
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
import java.util.*;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

@Service
@Slf4j
@RequiredArgsConstructor
public class SavePointOfSaleServiceImpl implements SavePointOfSaleService {

  private final MerchantService merchantService;
  private final PointOfSaleRepository pointOfSaleRepository;
  private final KeycloakService keycloakService;
  private final PointOfSaleDTOMapper mapper;
  private final PointOfSalesInitiativeRepository pointOfSalesInitiativeRepository;

  @Override
  public void savePointOfSales(String merchantId, String initiativeId, List<PointOfSaleDTO> dtos) {

    merchantService.verifyMerchantExists(merchantId);

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
}
