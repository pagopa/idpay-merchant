package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.utils.Utilities;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class PointOfSaleServiceImpl implements PointOfSaleService {

  private final MerchantService merchantService;
  private final PointOfSaleRepository pointOfSaleRepository;
  private final PointOfSaleDTOMapper pointOfSaleDTOMapper;
  private final PointOfSaleValidator pointOfSaleValidator;

  private final Keycloak keycloakAdminClient;
  private final String realm;

  public PointOfSaleServiceImpl(
      MerchantService merchantService,
      PointOfSaleRepository pointOfSaleRepository,
      PointOfSaleDTOMapper pointOfSaleDTOMapper,
      PointOfSaleValidator pointOfSaleValidator,
      Keycloak keycloakAdminClient,
      @Value("${keycloak.admin.realm}") String realm) {
    this.merchantService = merchantService;
    this.pointOfSaleRepository = pointOfSaleRepository;
    this.pointOfSaleDTOMapper = pointOfSaleDTOMapper;
    this.pointOfSaleValidator = pointOfSaleValidator;
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
  }

  @Override
  public void savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSaleDTOList){
    checkMerchantExist(merchantId);
    pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOList);

    List<PointOfSale> pointOfSales = pointOfSaleDTOList.stream()
        .map(pointOfSaleDTO -> pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,merchantId))
        .toList();

    pointOfSaleRepository.saveAll(pointOfSales);

    // For each POS, handle the verification of the ref. user and creation.
    pointOfSales.forEach(this::manageReferentUserOnKeycloak);
  }

  @Override
  public PointOfSaleListDTO getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable) {
    checkMerchantExist(merchantId);

    Criteria criteria = pointOfSaleRepository.getCriteria(merchantId, type, city, address, contactName);

    List<PointOfSale> entities = pointOfSaleRepository.findByFilter(criteria, pageable);
    long count = pointOfSaleRepository.getCount(criteria);

    final Page<PointOfSale> entitiesPage = PageableExecutionUtils.getPage(entities,
        Utilities.getPageable(pageable), () -> count);

    Page<PointOfSaleDTO> result = entitiesPage.map(pointOfSaleDTOMapper::pointOfSaleEntityToPointOfSaleDTO);

    return PointOfSaleListDTO.builder()
        .content(result.getContent())
        .pageNo(result.getNumber())
        .pageSize(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .build();
  }

  private void checkMerchantExist(String merchantId){
    MerchantDetailDTO merchantDetail = merchantService.getMerchantDetail(merchantId);
    if(merchantDetail == null){
      throw new MerchantNotFoundException(
          ExceptionCode.MERCHANT_NOT_ONBOARDED,
          String.format(ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE,merchantId));
    }
  }

  private void manageReferentUserOnKeycloak(PointOfSale pointOfSale) {

    final String contactEmail = pointOfSale.getContactEmail();

    if (!StringUtils.hasText(contactEmail)) {
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
        Integer lifespan = 120;
        usersResource.get(userId).executeActionsEmail(List.of("UPDATE_PASSWORD"), lifespan);

      } else {
        // Handling non-success cases with a log
        log.error("[KEYCLOAK] Failed to create Keycloak user for email {}. Status: {}, Reason: {}. Response body: {}",
            email, response.getStatus(), response.getStatusInfo().getReasonPhrase(), response.readEntity(String.class));
      }
    } catch (Exception e) {
      log.error("[KEYCLOAK] An exception occurred while creating Keycloak user for email {}", email, e);
    }
  }
}
