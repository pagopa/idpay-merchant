package it.gov.pagopa.merchant.service;

import feign.FeignException;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.*;
import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import it.gov.pagopa.merchant.exception.custom.InitiativeInvocationException;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.mapper.MerchantCreateDTOMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.merchant.*;
import it.gov.pagopa.merchant.utils.Utilities;
import it.gov.pagopa.merchant.utils.validator.MerchantValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class MerchantServiceImpl implements MerchantService {


  private final MerchantDetailService merchantDetailService;
  private final MerchantListService merchantListService;
  private final MerchantProcessOperationService merchantProcessOperationService;
  private final MerchantUpdatingInitiativeService merchantUpdatingInitiativeService;
  private final MerchantUpdateIbanService merchantUpdateIbanService;
  private final MerchantRepository merchantRepository;
  private final UploadingMerchantService uploadingMerchantService;
  private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper;
  private final List<String> defaultInitiatives;
  private final InitiativeRestConnector initiativeRestConnector;
  private final MerchantCreateDTOMapper merchantCreateDTOMapper;
  private final PointOfSaleRepository pointOfSaleRepository;
  private final MerchantValidator merchantValidator;
  private final Keycloak keycloakAdminClient;
  private final String realm;

  public MerchantServiceImpl(MerchantDetailService merchantDetailService,
      MerchantListService merchantListService,
      MerchantProcessOperationService merchantProcessOperationService,
      MerchantUpdatingInitiativeService merchantUpdatingInitiativeService,
      MerchantUpdateIbanService merchantUpdateIbanService, MerchantRepository merchantRepository,
      UploadingMerchantService uploadingMerchantService,
      Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper,
      @Value("${merchant.default-initiatives}") List<String> defaultInitiatives,
      InitiativeRestConnector initiativeRestConnector,
      MerchantCreateDTOMapper merchantCreateDTOMapper, PointOfSaleRepository pointOfSaleRepository,
      MerchantValidator merchantValidator, Keycloak keycloakAdminClient,
      @Value("${keycloak.admin.realm}") String realm) {
    this.merchantDetailService = merchantDetailService;
    this.merchantListService = merchantListService;
    this.merchantProcessOperationService = merchantProcessOperationService;
    this.merchantUpdatingInitiativeService = merchantUpdatingInitiativeService;
    this.merchantUpdateIbanService = merchantUpdateIbanService;
    this.merchantRepository = merchantRepository;
    this.uploadingMerchantService = uploadingMerchantService;
    this.initiative2InitiativeDTOMapper = initiative2InitiativeDTOMapper;
    this.defaultInitiatives = defaultInitiatives;
    this.initiativeRestConnector = initiativeRestConnector;
    this.merchantCreateDTOMapper = merchantCreateDTOMapper;
    this.pointOfSaleRepository = pointOfSaleRepository;
    this.merchantValidator = merchantValidator;
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
  }

  @Override
  public MerchantUpdateDTO uploadMerchantFile(MultipartFile file, String organizationId,
      String initiativeId, String organizationUserId, String acquirerId) {
    return uploadingMerchantService.uploadMerchantFile(file, organizationId, initiativeId,
        organizationUserId, acquirerId);
  }

  @Override
  public MerchantDetailDTO getMerchantDetail(String organizationId, String initiativeId,
      String merchantId) {
    return merchantDetailService.getMerchantDetail(organizationId, initiativeId, merchantId);
  }

  @Override
  public MerchantDetailDTO getMerchantDetail(String merchantId, String initiativeId) {
    return merchantDetailService.getMerchantDetail(merchantId, initiativeId);
  }

  @Override
  public MerchantDetailDTO getMerchantDetail(String merchantId) {
    return merchantDetailService.getMerchantDetail(merchantId);
  }


  @Override
  public MerchantListDTO getMerchantList(String organizationId, String initiativeId,
      String fiscalCode, Pageable pageable) {
    return merchantListService.getMerchantList(organizationId, initiativeId, fiscalCode, pageable);
  }

  @Override
  public String retrieveMerchantId(String acquirerId, String fiscalCode) {
    return merchantRepository.retrieveByAcquirerIdAndFiscalCode(acquirerId, fiscalCode)
        .map(Merchant::getMerchantId).orElse(null);
  }

  @Override
  public MerchantDetailDTO updateIban(String merchantId, String organizationId, String initiativeId,
      MerchantIbanPatchDTO merchantIbanPatchDTO) {
    return merchantUpdateIbanService.updateIban(merchantId, organizationId, initiativeId,
        merchantIbanPatchDTO);
  }

  @Override
  public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
    Optional<Merchant> merchant = merchantRepository.findById(merchantId);

    return merchant.map(value -> value.getInitiativeList().stream()
        .filter(i -> MerchantConstants.INITIATIVE_PUBLISHED.equals(i.getStatus()))
        .map(initiative2InitiativeDTOMapper::apply).toList()).orElse(Collections.emptyList());
  }

  @Override
  public void processOperation(QueueCommandOperationDTO queueCommandOperationDTO) {
    merchantProcessOperationService.processOperation(queueCommandOperationDTO);
  }

  @Override
  public void updatingInitiative(QueueInitiativeDTO queueInitiativeDTO) {
    merchantUpdatingInitiativeService.updatingInitiative(queueInitiativeDTO);
  }

  @Override
  public MerchantWithdrawalResponse deactivateMerchant(String merchantId, String initiativeId, boolean dryRun) {
    Merchant merchant = merchantRepository
        .retrieveByMerchantIdAndInitiativeId(merchantId, initiativeId)
        .orElseThrow(() -> new MerchantNotFoundException(
            String.format("Merchant %s not found for initiative %s", merchantId, initiativeId)));

    List<PointOfSale> pointsOfSale = pointOfSaleRepository.findByMerchantId(merchantId);

    merchantValidator.validateMerchantWithdrawal(merchant, initiativeId);

    if (dryRun) {
      log.info("[MERCHANT-WITHDRAWAL] Dry-run mode: merchant {} for initiative {} passed all validations", sanitizeString(merchantId), sanitizeString(initiativeId));
      return new MerchantWithdrawalResponse(
          String.format("Merchant %s can be safely deactivated for initiative %s and associated points of sale can be deleted.",
              merchantId, initiativeId)
      );
    }

    deleteKeycloakUsers(pointsOfSale);
    pointOfSaleRepository.deleteByMerchantId(merchantId);
    merchant.setEnabled(false);
    merchantRepository.save(merchant);

    log.info("[MERCHANT-WITHDRAWAL] Disabled merchant {} for initiative {} and removed points of sale", sanitizeString(merchantId), sanitizeString(initiativeId));

    return new MerchantWithdrawalResponse(
        String.format("Merchant %s has been deactivated for initiative %s. Associated points of sale have been successfully deleted.",
            merchantId, initiativeId)
    );
  }

  @Override
  public String retrieveOrCreateMerchantIfNotExists(MerchantCreateDTO merchantCreateDTO) {

    Optional<Merchant> existingMerchantOpt = merchantRepository.findByFiscalCode(merchantCreateDTO.getFiscalCode());
    if (existingMerchantOpt.isPresent()) {
      Merchant existingMerchant = existingMerchantOpt.get();

      // Update IBAN, IBAN holder and businessName
      updateMerchant(existingMerchant, merchantCreateDTO);

      // Save updated entity
      merchantRepository.save(existingMerchant);
      log.info("[UPDATE_MERCHANT] Merchant with merchantId={} successfully updated", existingMerchant.getMerchantId());
      return existingMerchant.getMerchantId();

    }else {
      String merchantId = createNewMerchant(merchantCreateDTO);
      log.info("[CREATE_MERCHANT] Merchant with merchantId={} successfully created", merchantId);
      return merchantId;
    }
  }

  private void deleteKeycloakUsers(List<PointOfSale> pointsOfSale) {
    UsersResource usersResource = keycloakAdminClient.realm(realm).users();

    for (PointOfSale pos : pointsOfSale) {
      String email = pos.getContactEmail();
      if (email == null || email.isEmpty()) {
        continue;
      }

      List<UserRepresentation> users = usersResource.searchByEmail(email, true);
      for (UserRepresentation user : users) {
        try {
          usersResource.get(user.getId()).logout();
          usersResource.get(user.getId()).remove();
          log.info("[KEYCLOAK] Deleted user for email {}", email);
        } catch (Exception ex) {
          log.error("[KEYCLOAK] Failed to delete user for email {}: {}", email, ex.getMessage(), ex);
        }
      }
    }
  }

  private void updateMerchant(Merchant existingMerchant, MerchantCreateDTO merchantCreateDTO) {

    if(StringUtils.isNotBlank(merchantCreateDTO.getIban())){
      existingMerchant.setIban(merchantCreateDTO.getIban());
    }
    if(StringUtils.isNotBlank(merchantCreateDTO.getBusinessName())){
      existingMerchant.setBusinessName(merchantCreateDTO.getBusinessName());
    }
    if(StringUtils.isNotBlank(merchantCreateDTO.getIbanHolder())){
      existingMerchant.setIbanHolder(merchantCreateDTO.getIbanHolder());
    }
    if(merchantCreateDTO.getActivationDate()!=null){
      existingMerchant.setActivationDate(merchantCreateDTO.getActivationDate());
    }
  }

  private String createNewMerchant(MerchantCreateDTO merchantCreateDTO) {
    String merchantId = Utilities.toUUID(merchantCreateDTO.getFiscalCode().concat("_").concat(merchantCreateDTO.getAcquirerId()));
    List<Initiative> initiatives = new ArrayList<>();
    for (String initiativeId : defaultInitiatives) {
      InitiativeBeneficiaryViewDTO dto = getInitiativeInfo(initiativeId);
      initiatives.add(createMerchantInitiative(dto));
    }

    Merchant merchant = merchantCreateDTOMapper.dtoToEntity(merchantCreateDTO, merchantId);
    merchant.setInitiativeList(initiatives);
    merchant.setEnabled(true);
    merchantRepository.save(merchant);
    return merchantId;
  }

  private InitiativeBeneficiaryViewDTO getInitiativeInfo(String initiativeId) {
    InitiativeBeneficiaryViewDTO initiativeDTO;
    try {
      initiativeDTO = initiativeRestConnector.getInitiativeBeneficiaryView(initiativeId);
    } catch (FeignException e) {
      log.error("[INITIATIVE REST CONNECTOR] - Feign exception: {}", e.getMessage());
      throw new InitiativeInvocationException(
          MerchantConstants.ExceptionMessage.INITIATIVE_CONNECTOR_ERROR);
    }

    if (initiativeDTO == null) {
      log.error("[INITIATIVE REST CONNECTOR] Initiative returned null for id={}", initiativeId);
      throw new InitiativeInvocationException("Initiative not found for id=" + initiativeId);
    }

    return initiativeDTO;
  }

  private Initiative createMerchantInitiative(InitiativeBeneficiaryViewDTO dto) {
    return Initiative.builder().initiativeId(dto.getInitiativeId())
        .initiativeName(dto.getInitiativeName()).organizationId(dto.getOrganizationId())
        .organizationName(dto.getOrganizationName())
        .serviceId(dto.getAdditionalInfo().getServiceId())
        .startDate(dto.getGeneral().getStartDate()).endDate(dto.getGeneral().getEndDate())
        .status(dto.getStatus()).merchantStatus("UPLOADED").creationDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now()).enabled(true).build();
  }
}