package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.exception.custom.PosValidationException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.repository.PointOfSalesInitiativeRepository;
import it.gov.pagopa.merchant.service.KeycloakService;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class SavePointOfSaleServiceImpl implements SavePointOfSaleService {

  private final MerchantService merchantService;
  private final PointOfSaleRepository repository;
  private final KeycloakService keycloakService;
  private final PointOfSaleDTOMapper mapper;
  private final PointOfSalesInitiativeRepository initiativeRepository;

  public SavePointOfSaleServiceImpl(
          MerchantService merchantService,
          PointOfSaleRepository repository,
          KeycloakService keycloakService,
          PointOfSaleDTOMapper mapper,
          PointOfSalesInitiativeRepository initiativeRepository) {

    this.merchantService = merchantService;
    this.repository = repository;
    this.keycloakService = keycloakService;
    this.mapper = mapper;
    this.initiativeRepository = initiativeRepository;
  }

  @Override
  public void savePointOfSales(String merchantId, String initiativeId, List<PointOfSaleDTO> dtos) {

    merchantService.verifyMerchantExists(merchantId);

    List<PointOfSale> entities = dtos.stream()
            .map(dto -> mapper.dtoToEntity(dto, merchantId))
            .toList();

    List<ValidationErrorDetail> errors = validate(entities, dtos, merchantId, initiativeId);

    if (!errors.isEmpty()) {
      throw new PosValidationException(errors);
    }

    Set<PointOfSale> saved = new HashSet<>();
    Set<PointOfSalesInitiative> associations = new HashSet<>();

    try {

      for (int i = 0; i < entities.size(); i++) {

        PointOfSale entity = entities.get(i);
        PointOfSaleDTO dto = dtos.get(i);

        PointOfSale persisted = repository.save(entity);

        keycloakService.manageReferentUserOnKeycloak(persisted, dto.getContactEmail());

        saved.add(persisted);

        associations.add(buildAssociation(persisted.getId(), merchantId, initiativeId));
      }

      initiativeRepository.saveAll(associations);

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

      int finalI = i;
      repository.findByContactEmail(dto.getContactEmail())
              .ifPresent(e -> errors.add(emailError(finalI, dto.getContactEmail())));

      repository.findDuplicate(entity).ifPresent(existing -> {

        boolean sameInitiative = initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                        existing.getId(), initiativeId, merchantId)
                .isPresent();

        errors.add(posError(finalI, dto, existing, sameInitiative));
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

      List<String> ids = saved.stream().map(PointOfSale::getId).toList();

      initiativeRepository.deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(
              merchantId, initiativeId, ids);

      saved.forEach(p -> repository.deleteById(p.getId()));

    } catch (Exception ex) {
      log.error("[COMPENSATION] failed", ex);
    }
  }

  private RuntimeException handle(Exception ex) {

    if (ex instanceof DuplicateKeyException) {
      return new ServiceException(PointOfSaleConstants.CODE_ALREADY_REGISTERED, "duplicate");
    }

    if (ex instanceof IncorrectResultSizeDataAccessException) {
      return new ServiceException(PointOfSaleConstants.CODE_DATA_INCONSISTENCY, "inconsistent DB");
    }

    return new ServiceException(PointOfSaleConstants.CODE_GENERIC_SAVE_ERROR,
            PointOfSaleConstants.MSG_GENERIC_SAVE_ERROR);
  }

  private PointOfSalesInitiative buildAssociation(String posId, String merchantId, String initiativeId) {
    return PointOfSalesInitiative.builder()
            .pointOfSaleId(posId)
            .merchantId(merchantId)
            .initiativeId(initiativeId)
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
  }
}