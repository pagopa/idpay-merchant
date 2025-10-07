package it.gov.pagopa.merchant.service.pointofsales;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleDuplicateException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.utils.Utilities;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PointOfSaleServiceImpl implements PointOfSaleService {

  private final MerchantService merchantService;
  private final PointOfSaleRepository pointOfSaleRepository;

  private final Keycloak keycloakAdminClient;
  private final String realm;
  private final String redirectURI;
  private final String keycloakClientId;
  private final Integer keycloakUserActionsEmailLifespan;

  private static final String REQUIRED_ACTION_UPDATE_PASSWORD = "UPDATE_PASSWORD";

  public PointOfSaleServiceImpl(
          MerchantService merchantService,
          PointOfSaleRepository pointOfSaleRepository,
          Keycloak keycloakAdminClient,
          @Value("${keycloak.admin.realm}") String realm,
          @Value("${keycloak.admin.user.actions.email.lifespan}") Integer keycloakUserActionsEmailLifespan,
          @Value("${keycloak.admin.redirect-uri}") String redirectURI,
          @Value("${keycloak.admin.redirect-client-id}") String keycloakClientId) {
    this.merchantService = merchantService;
    this.pointOfSaleRepository = pointOfSaleRepository;
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
    this.redirectURI = redirectURI;
    this.keycloakUserActionsEmailLifespan = keycloakUserActionsEmailLifespan;
    this.keycloakClientId = keycloakClientId;
  }

  @Override
  public void savePointOfSales(String merchantId, List<PointOfSale> pointOfSales) {

    verifyMerchantExists(merchantId);

    List<PointOfSaleUpdateContext> entities = pointOfSales.stream()
            .map(this::preparePointOfSaleForSave)
            .toList();

    List<PointOfSale> savedPointOfSales = new ArrayList<>();
    String currentEmail = "";
    try {
      for (PointOfSaleUpdateContext entity : entities) {
        currentEmail = entity.pointOfSale().getContactEmail();
        PointOfSale saved = pointOfSaleRepository.save(entity.pointOfSale());
        savedPointOfSales.add(saved);
        manageReferentUserOnKeycloak(saved, entity.oldEmail());
      }
    } catch (Exception exception) {
      log.error(
              "[POINT-OF-SALES][SAVE] Error during saving PointOfSales. Initiating compensation rollback.");
      compensatingDelete(savedPointOfSales);
      log.error("[POINT-OF-SALES][SAVE] Compensation rollback completed.");
      if (exception instanceof DuplicateKeyException) {
        throw new PointOfSaleDuplicateException(currentEmail);
      }
      log.error("[POINT-OF-SALES][SAVE] Exception message: {}", exception.getMessage());
      throw new ServiceException(PointOfSaleConstants.CODE_GENERIC_SAVE_ERROR,
              PointOfSaleConstants.MSG_GENERIC_SAVE_ERROR);
    }
  }

  private void compensatingDelete(List<PointOfSale> savedEntities) {
    for (PointOfSale pointOfSale : savedEntities) {
      try {
        pointOfSaleRepository.deleteById(pointOfSale.getId());
        UsersResource usersResource = keycloakAdminClient.realm(realm).users();
        List<UserRepresentation> existingUsers = usersResource.searchByEmail(pointOfSale.getContactEmail(), true);
        for (UserRepresentation user : existingUsers) {
          usersResource.get(user.getId()).remove();
        }
      } catch (Exception exception) {
        log.error("[POINT-OF-SALES][COMPENSATION] Failed to delete Point of sale with id: {}",
                sanitizeForLog(pointOfSale.getId()));
      }
    }
  }

  @Override
  public Page<PointOfSale> getPointOfSalesList(
          String merchantId,
          String type,
          String city,
          String address,
          String contactName,
          Pageable pageable) {

    verifyMerchantExists(merchantId);

    Criteria criteria = pointOfSaleRepository.getCriteria(merchantId, type, city, address,
            contactName);
    List<PointOfSale> matched = pointOfSaleRepository.findByFilter(criteria, pageable);
    long total = pointOfSaleRepository.getCount(criteria);

    return PageableExecutionUtils.getPage(matched, Utilities.getPageable(pageable), () -> total);
  }

  /**
   * Prepares the PointOfSale entity for insert or update
   * <p>
   * If the PointOfSale already exists (determined by the presence of an ID), it preserves the
   * original creation date. Also checks if there is any existing PointOfSale with the same contact
   * email and throws a {@link PointOfSaleDuplicateException if found}
   * </p>
   *
   * @param pointOfSale the PointOfSale entity to prepare
   * @return the prepared PointOfSale entity for persistence
   * @throws PointOfSaleDuplicateException if a PointOfSale with the same contact email already
   *                                       exists
   */
  private PointOfSaleUpdateContext preparePointOfSaleForSave(PointOfSale pointOfSale) {
    String id = pointOfSale.getId();
    String oldEmail = null;

    boolean isInsert = StringUtils.isNotEmpty(id);
    if (isInsert) {
      PointOfSale pointOfSaleExisting = getPointOfSaleById(id);
      pointOfSale.setCreationDate(pointOfSaleExisting.getCreationDate());

      oldEmail = pointOfSaleExisting.getContactEmail();
    }

    return new PointOfSaleUpdateContext(pointOfSale, oldEmail);
  }


  /**
   * Verifies if the merchant exists in the system.
   *
   * @param merchantId the ID of the merchant to check
   * @throws MerchantNotFoundException if the merchant does not exist
   */
  private void verifyMerchantExists(String merchantId) {
    MerchantDetailDTO merchantDetail = merchantService.getMerchantDetail(merchantId);
    if (merchantDetail == null) {
      throw new MerchantNotFoundException(
              String.format(MerchantConstants.ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE, merchantId));
    }
  }

  public PointOfSale getPointOfSaleById(String pointOfSaleId) {
    return pointOfSaleRepository.findById(pointOfSaleId)
            .orElseThrow(() -> new PointOfSaleNotFoundException(
                    String.format(PointOfSaleConstants.MSG_NOT_FOUND, pointOfSaleId)));
  }

  @Override
  public PointOfSale getPointOfSaleByIdAndMerchantId(String pointOfSaleId, String merchantId) {
    verifyMerchantExists(merchantId);

    return pointOfSaleRepository.findByIdAndMerchantId(pointOfSaleId, merchantId)
            .orElseThrow(() -> new PointOfSaleNotFoundException(
                    String.format(PointOfSaleConstants.MSG_NOT_FOUND, pointOfSaleId)
            ));
  }

  private void manageReferentUserOnKeycloak(PointOfSale pointOfSale, String oldEmail) {
    final String contactEmail = pointOfSale.getContactEmail();

    if (StringUtils.isEmpty(contactEmail)) {
      log.warn(
              "[KEYCLOAK] Point of Sale with ID {} for merchant {} has no contact email. Skipping Keycloak user creation.",
              sanitizeForLog(pointOfSale.getId()), sanitizeForLog(pointOfSale.getMerchantId()));
      return;
    }

    UsersResource usersResource = keycloakAdminClient.realm(realm).users();

    try {
      deleteOldUser(usersResource, oldEmail, contactEmail);
      handleNewOrExistingUser(usersResource, pointOfSale, contactEmail);
    } catch (Exception e) {
      log.error(
              "[KEYCLOAK] Error while creating Keycloak user for Point of Sale with ID {}. Exception: {}",
              pointOfSale.getId(), e.getMessage(), e);
    }
  }

  private void deleteOldUser(UsersResource usersResource, String oldEmail, String newEmail) {
    if (StringUtils.isNotEmpty(oldEmail) && !oldEmail.equalsIgnoreCase(newEmail)) {
      List<UserRepresentation> existingUsers = usersResource.searchByEmail(oldEmail, true);
      for (UserRepresentation user : existingUsers) {
        usersResource.get(user.getId()).logout();
        usersResource.get(user.getId()).remove();
        log.info("[KEYCLOAK] Logged out and deleted user with email: {}", oldEmail);
      }
    }
  }

  private void handleNewOrExistingUser(UsersResource usersResource, PointOfSale pointOfSale,
                                       String contactEmail) {
    List<UserRepresentation> existingUsers = usersResource.searchByEmail(contactEmail, true);

    if (existingUsers.isEmpty()) {
      createNewUserAndSendActionsEmail(usersResource, pointOfSale);
    } else {
      updateEnabledUsers(usersResource, pointOfSale, contactEmail, existingUsers);
      log.info(
              "[KEYCLOAK] User already exists and is enabled. The new Point of Sale with ID {} will be associated with the existing user.",
              sanitizeForLog(pointOfSale.getId()));
    }
  }

  private void updateEnabledUsers(UsersResource usersResource, PointOfSale pointOfSale,
                                  String contactEmail, List<UserRepresentation> users) {
    for (UserRepresentation user : users) {
      user.setFirstName(pointOfSale.getContactName());
      user.setLastName(pointOfSale.getContactSurname());
      usersResource.get(user.getId()).update(user);
      log.info("[KEYCLOAK] Updated contact name/surname for existing enabled user with email: {}",
              sanitizeForLog(contactEmail));
    }
  }

  private void createNewUserAndSendActionsEmail(UsersResource usersResource,
                                                PointOfSale pointOfSale) {
    UserRepresentation newUser = new UserRepresentation();
    newUser.setEmail(pointOfSale.getContactEmail());
    newUser.setUsername(pointOfSale.getContactEmail());
    newUser.setFirstName(pointOfSale.getContactName());
    newUser.setLastName(pointOfSale.getContactSurname());

    newUser.setEnabled(true);
    newUser.setEmailVerified(true);

    // Custom attrs
    Map<String, List<String>> attrs = new HashMap<>();
    if(StringUtils.isNotEmpty(pointOfSale.getMerchantId())){
      attrs.put("merchantId", List.of(pointOfSale.getMerchantId()));
    }
    if(StringUtils.isNotEmpty(pointOfSale.getId())){
      attrs.put("pointOfSaleId", List.of(pointOfSale.getId()));
    }
    newUser.setAttributes(attrs);

    log.info("[KEYCLOAK] Attempting to create a new Keycloak user linked to Point of Sale ID {}",
            sanitizeForLog(pointOfSale.getId()));

    try (Response response = usersResource.create(newUser)) {
      if (response.getStatus() == Response.Status.CREATED.getStatusCode()) { // Status code 201
        String userId = CreatedResponseUtil.getCreatedId(response);
        log.info("[KEYCLOAK] User created successfully with ID {}. Sending password setup email.",
                userId);

        // The action "UPDATE_PASSWORD" sends an email with a link that will expire after the lifespan to reset the user password
        usersResource.get(userId)
                .executeActionsEmail(keycloakClientId, redirectURI, keycloakUserActionsEmailLifespan,
                        List.of(REQUIRED_ACTION_UPDATE_PASSWORD));

      } else {
        // Handling non-success cases with a log
        log.error("[KEYCLOAK] Failed to create Keycloak user. Status: {}, Reason: {}.",
                response.getStatus(), response.getStatusInfo().getReasonPhrase());
      }
    } catch (Exception e) {
      log.error("[KEYCLOAK] An exception occurred while creating Keycloak user.", e);
      throw e;
    }
  }

  private static String sanitizeForLog(String input) {
    if (input == null) {
      return null;
    }

    String sanitized = input.replaceAll("[\\p{Cntrl}\\u2028\\u2029]", "");
    sanitized = sanitized.replaceAll("[^a-zA-Z0-9@._-]", "_");
    return sanitized.trim();
  }

  private record PointOfSaleUpdateContext(PointOfSale pointOfSale, String oldEmail) {

  }

}
