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
import org.bson.types.ObjectId;
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
import java.util.List;

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

  public PointOfSaleServiceImpl(
      MerchantService merchantService,
      PointOfSaleRepository pointOfSaleRepository,
      Keycloak keycloakAdminClient,
      @Value("${keycloak.admin.realm}") String realm,
      @Value("${keycloak.admin.user.actions.email.lifespan}") Integer keycloakUserActionsEmailLifespan,
      @Value("${keycloak.admin.redirect-uri}") String redirectURI,
      @Value("${keycloak.admin.client-id}") String keycloakClientId) {
    this.merchantService = merchantService;
    this.pointOfSaleRepository = pointOfSaleRepository;
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
    this.redirectURI = redirectURI;
    this.keycloakUserActionsEmailLifespan = keycloakUserActionsEmailLifespan;
    this.keycloakClientId = keycloakClientId;
  }

  @Override
  public void savePointOfSales(String merchantId, List<PointOfSale> pointOfSales){

    verifyMerchantExists(merchantId);

    List<PointOfSale> entities = pointOfSales.stream()
        .map(this::preparePointOfSaleForSave)
        .toList();

    List<PointOfSale> savedPointOfSales = new ArrayList<>();

    try{
      for(PointOfSale entity : entities){
        PointOfSale saved = pointOfSaleRepository.save(entity);
        savedPointOfSales.add(saved);
        manageReferentUserOnKeycloak(saved);
      }
    }
    catch (Exception exception){
      log.error("[POINT-OF-SALES][SAVE] Error during saving PointOfSales. Initiating compensation rollback.");
      compensatingDelete(savedPointOfSales);
      log.error("[POINT-OF-SALES][SAVE] Compensation rollback completed.");
      if(exception instanceof DuplicateKeyException){
        throw new PointOfSaleDuplicateException(PointOfSaleConstants.MSG_ALREADY_REGISTERED);
      }
      throw new ServiceException(PointOfSaleConstants.CODE_GENERIC_SAVE_ERROR,PointOfSaleConstants.MSG_GENERIC_SAVE_ERROR);
    }
  }

  private void compensatingDelete(List<PointOfSale> savedEntities){
    for(PointOfSale pointOfSale : savedEntities){
      try{
        pointOfSaleRepository.deleteById(pointOfSale.getId().toString());
      }catch (Exception exception){
        log.error("[POINT-OF-SALES][COMPENSATION] Failed to delete Point of sale with id: {}", pointOfSale.getId().toString());
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

    Criteria criteria = pointOfSaleRepository.getCriteria(merchantId, type, city, address, contactName);
    List<PointOfSale> matched = pointOfSaleRepository.findByFilter(criteria, pageable);
    long total = pointOfSaleRepository.getCount(criteria);

    return PageableExecutionUtils.getPage(matched, Utilities.getPageable(pageable), () -> total);
  }

  /**
   * Prepares the PointOfSale entity for insert or uprdate
   * <p>
   *     If the PointOfSale already exists (determined by the presence of an ID),
   *     it preserves the original creation date.
   *     Also checks if there is any existing PointOfSale with the same contact email
   *     and throws a {@link PointOfSaleDuplicateException if found}
   * </p>
   *
   * @param pointOfSale the PointOfSale entity to prepare
   * @return the prepared PointOfSale entity for persistance
   * @throws PointOfSaleDuplicateException if a PointOfSale with the same contact email already exists
   */
  private PointOfSale preparePointOfSaleForSave(PointOfSale pointOfSale){
    ObjectId id = pointOfSale.getId();

    boolean isInsert = id != null && StringUtils.isNotEmpty(id.toString());
    if(isInsert){
      PointOfSale pointOfSaleExisting = getPointOfSaleById(id.toString());
      pointOfSale.setCreationDate(pointOfSaleExisting.getCreationDate());
    }

    return pointOfSale;
  }


  /**
   * Verifies if the merchant exists in the system.
   *
   * @param merchantId the ID of the merchant to check
   * @throws MerchantNotFoundException if the merchant does not exist
   */
  private void verifyMerchantExists(String merchantId){
    MerchantDetailDTO merchantDetail = merchantService.getMerchantDetail(merchantId);
    if(merchantDetail == null){
      throw new MerchantNotFoundException(String.format(MerchantConstants.ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE,merchantId));
    }
  }

  public PointOfSale getPointOfSaleById(String pointOfSaleId){
    return pointOfSaleRepository.findById(pointOfSaleId)
        .orElseThrow(() -> new PointOfSaleNotFoundException(String.format(PointOfSaleConstants.MSG_NOT_FOUND,pointOfSaleId)));
  }


  private void manageReferentUserOnKeycloak(PointOfSale pointOfSale) {

    final String contactEmail = pointOfSale.getContactEmail();

    if (StringUtils.isEmpty(contactEmail)) {
      log.warn("[KEYCLOAK] Point of Sale with ID {} for merchant {} has no contact email. Skipping Keycloak user creation.",
          pointOfSale.getId(), pointOfSale.getMerchantId());
      return;
    }

    UsersResource usersResource = keycloakAdminClient.realm(realm).users();
    List<UserRepresentation> existingUsers = usersResource.searchByEmail(contactEmail, true);

    if (existingUsers.isEmpty()) {
      createNewUserAndSendActionsEmail(usersResource, contactEmail, pointOfSale);
    } else {
      log.info("[KEYCLOAK] User with email {} already exists. The new Point of Sale with ID {} will be associated with the existing user.",
          contactEmail, pointOfSale.getId());
    }
  }

  private void createNewUserAndSendActionsEmail(UsersResource usersResource, String email, PointOfSale pointOfSale) {
    UserRepresentation newUser = new UserRepresentation();
    newUser.setEmail(email);
    newUser.setUsername(email);
    newUser.setEnabled(true);
    newUser.setEmailVerified(true);

    log.info("[KEYCLOAK] Attempting to create a new Keycloak user for email {} linked to Point of Sale ID {}",
        email, pointOfSale.getId());

    try (Response response = usersResource.create(newUser)) {
      if (response.getStatus() == Response.Status.CREATED.getStatusCode()) { // Status code 201
        String userId = CreatedResponseUtil.getCreatedId(response);
        log.info("[KEYCLOAK] User {} created successfully with ID {}. Sending password setup email.", email, userId);

        // The action "UPDATE_PASSWORD" sends an email with a link that will expire after the lifespan to reset the user password
        usersResource.get(userId).executeActionsEmail(keycloakClientId, redirectURI, keycloakUserActionsEmailLifespan, List.of("UPDATE_PASSWORD"));

      } else {
        // Handling non-success cases with a log
        log.error("[KEYCLOAK] Failed to create Keycloak user for email {}. Status: {}, Reason: {}. Response body: {}",
            email, response.getStatus(), response.getStatusInfo().getReasonPhrase(), response.readEntity(String.class));
      }
    } catch (Exception e) {
      log.error("[KEYCLOAK] An exception occurred while creating Keycloak user for email {}", email, e);
      throw e;
    }
  }
}
