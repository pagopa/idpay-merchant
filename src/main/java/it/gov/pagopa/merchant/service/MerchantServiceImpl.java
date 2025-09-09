package it.gov.pagopa.merchant.service;

import feign.FeignException;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.*;
import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import it.gov.pagopa.merchant.exception.custom.InitiativeInvocationException;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.mapper.MerchantCreateDTOMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.service.merchant.*;
import it.gov.pagopa.merchant.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

  public MerchantServiceImpl(MerchantDetailService merchantDetailService,
      MerchantListService merchantListService,
      MerchantProcessOperationService merchantProcessOperationService,
      MerchantUpdatingInitiativeService merchantUpdatingInitiativeService,
      MerchantUpdateIbanService merchantUpdateIbanService, MerchantRepository merchantRepository,
      UploadingMerchantService uploadingMerchantService,
      Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper,
      @Value("${merchant.default-initiatives}") List<String> defaultInitiatives,
      InitiativeRestConnector initiativeRestConnector,
      MerchantCreateDTOMapper merchantCreateDTOMapper) {
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
  public String retrieveOrCreateMerchantIfNotExists(MerchantCreateDTO merchantCreateDTO) {

    Optional<Merchant> existingMerchantOpt = merchantRepository.findByFiscalCode(merchantCreateDTO.getFiscalCode());
    if (existingMerchantOpt.isPresent()) {
      Merchant existingMerchant = existingMerchantOpt.get();

      // Update IBAN, IBAN holder and businessName
      updateMerchant(existingMerchant, merchantCreateDTO);

      // Save updated entity
      merchantRepository.save(existingMerchant);

      return existingMerchant.getMerchantId();

    }else {
      return createNewMerchant(merchantCreateDTO);
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