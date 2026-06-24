package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleDuplicateException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.repository.PointOfSalesInitiativeRepository;
import it.gov.pagopa.merchant.service.KeycloakService;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeForLog;

@Service
@Slf4j
public class SavePointOfSaleServiceImpl implements SavePointOfSaleService {

  private final MerchantService merchantService;
  private final PointOfSaleRepository pointOfSaleRepository;
  private final KeycloakService keycloakService;
  private final PointOfSaleDTOMapper pointOfSaleDTOMapper;
  private final PointOfSalesInitiativeRepository pointOfSalesInitiativeRepository;

  public SavePointOfSaleServiceImpl(
          MerchantService merchantService,
          PointOfSaleRepository pointOfSaleRepository,
          KeycloakService keycloakService,
          PointOfSaleDTOMapper pointOfSaleDTOMapper,
          PointOfSalesInitiativeRepository pointOfSalesInitiativeRepository) {

    this.merchantService = merchantService;
    this.pointOfSaleRepository = pointOfSaleRepository;
    this.keycloakService = keycloakService;
    this.pointOfSaleDTOMapper = pointOfSaleDTOMapper;
    this.pointOfSalesInitiativeRepository = pointOfSalesInitiativeRepository;
  }

  @Override
  public void savePointOfSales(String merchantId, String initiativeId, List<PointOfSaleDTO> pointOfSaleList) {
    merchantService.verifyMerchantExists(merchantId);

    Set<PointOfSale> savedPosSet = new HashSet<>();
    Set<PointOfSalesInitiative> associations = new HashSet<>();
    String currentEmail = "";

    try {
      for (PointOfSaleDTO dto : pointOfSaleList) {
        currentEmail = dto.getContactEmail();
        processDto(dto, merchantId, initiativeId, savedPosSet, associations);
      }

      if (!associations.isEmpty()) {
        pointOfSalesInitiativeRepository.saveAll(associations);
      }

    } catch (Exception exception) {
      log.error("[POINT-OF-SALES][SAVE] Error during saving POS. Starting compensation.");
      compensatingDeleteAssociations(savedPosSet, initiativeId, merchantId);
      compensatingDelete(savedPosSet);
      log.error("[POINT-OF-SALES][SAVE] Compensation completed.");

      handleException(exception, currentEmail);
    }

  }

  private static void handleException(Exception exception, String currentEmail) {
    if (exception instanceof DuplicateKeyException) {
      throw new PointOfSaleDuplicateException(currentEmail);
    }

    if (exception instanceof IncorrectResultSizeDataAccessException){
      log.error("[POINT-OF-SALES] Duplicate Point of Sales detected in DB for business key");
      throw new ServiceException(
              PointOfSaleConstants.CODE_DATA_INCONSISTENCY,
              "Multiple PointOfSale found for business key"
      );
    }

    if (exception instanceof PointOfSaleDuplicateException ex){
      throw ex;
    }

    log.error("[POINT-OF-SALES][SAVE] Exception: {}", exception.getMessage());

    throw new ServiceException(
            PointOfSaleConstants.CODE_GENERIC_SAVE_ERROR,
            PointOfSaleConstants.MSG_GENERIC_SAVE_ERROR
    );

  }

  private void processDto(PointOfSaleDTO dto, String merchantId, String initiativeId,  Set<PointOfSale> savedPosSet, Set<PointOfSalesInitiative> associations) {
    PointOfSale entity = pointOfSaleDTOMapper.dtoToEntity(dto, merchantId);
    PointOfSale duplicate = getDuplicatePointOfSale(entity);

    if (duplicate != null) {
      throw new PointOfSaleDuplicateException(duplicate.getContactEmail());
    } else {
      handleNewPOS(entity, dto.getContactEmail(), merchantId, initiativeId, savedPosSet, associations);
    }
  }

  private void handleNewPOS(PointOfSale entity, String contactEmail, String merchantId, String initiativeId, Set<PointOfSale> savedPosSet, Set<PointOfSalesInitiative> associations) {
    PointOfSale saved = resolvePointOfSale(entity, contactEmail);
    savedPosSet.add(saved);
    associations.add(buildPointOfSaleInitiative(saved.getId(), merchantId, initiativeId));
  }

  private PointOfSale resolvePointOfSale(PointOfSale entity, String email) {
    PointOfSale saved = pointOfSaleRepository.save(entity);

    keycloakService.manageReferentUserOnKeycloak(saved, email);

    return saved;
  }

  private PointOfSale getDuplicatePointOfSale(PointOfSale entity) {
    Optional<PointOfSale> existing = pointOfSaleRepository.findDuplicate(entity);

    return existing.orElse(null);
  }

  private PointOfSalesInitiative buildPointOfSaleInitiative(String posId, String merchantId, String initiativeId) {
    Instant now = Instant.now();

    return PointOfSalesInitiative.builder()
            .pointOfSaleId(posId)
            .initiativeId(initiativeId)
            .merchantId(merchantId)
            .enabled(true)
            .createdAt(now)
            .updatedAt(now)
            .build();
  }

  private void compensatingDelete(Set<PointOfSale> savedEntities) {
    for (PointOfSale pointOfSale : savedEntities) {
      try {
        pointOfSaleRepository.deleteById(pointOfSale.getId());
        UsersResource usersResource = keycloakService.getUserResource();
        List<UserRepresentation> existingUsers = usersResource.searchByEmail(pointOfSale.getContactEmail(), true);
        for (UserRepresentation user : existingUsers) {
          usersResource.get(user.getId()).remove();
        }
      } catch (Exception ex) {
        log.error("[POINT-OF-SALES][COMPENSATION] Failed to delete Point of sale with id: {}",
                sanitizeForLog(pointOfSale.getId()));
      }
    }
  }

  private void compensatingDeleteAssociations(Set<PointOfSale> posSet,  String initiativeId, String merchantId) {
    List<String> posIds = posSet.stream()
            .map(PointOfSale::getId)
            .toList();

    pointOfSalesInitiativeRepository
            .deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(
                    merchantId,
                    initiativeId,
                    posIds);
  }

}
