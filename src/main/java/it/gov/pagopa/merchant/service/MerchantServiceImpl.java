package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.initiative.InitiativeRestClient;
import it.gov.pagopa.merchant.connector.pdnd.PdndInfoCamereConnectorImpl;
import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import it.gov.pagopa.merchant.connector.transaction.dto.MerchantRewardBatchListDTO;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.*;
import it.gov.pagopa.merchant.dto.initiative.InitiativeResponse;
import it.gov.pagopa.merchant.dto.pdnd.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static it.gov.pagopa.common.utils.CommonConstants.ZONEID;
import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

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
  private final MerchantCreateDTOMapper merchantCreateDTOMapper;
  private final PointOfSaleRepository pointOfSaleRepository;
  private final MerchantValidator merchantValidator;
  private final Keycloak keycloakAdminClient;
  private final String realm;
  private final PdndInfoCamereConnectorImpl pdndConnector;
  private final InitiativeRestClient initiativeRestClient;
  private final TransactionConnector transactionConnector;

  private static final int REWARD_BATCH_PAGE_SIZE = 100;

  public MerchantServiceImpl(MerchantDetailService merchantDetailService,
                             MerchantListService merchantListService,
                             MerchantProcessOperationService merchantProcessOperationService,
                             MerchantUpdatingInitiativeService merchantUpdatingInitiativeService,
                             MerchantUpdateIbanService merchantUpdateIbanService,
                             MerchantRepository merchantRepository,
                             UploadingMerchantService uploadingMerchantService,
                             Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper,
                             MerchantCreateDTOMapper merchantCreateDTOMapper,
                             PointOfSaleRepository pointOfSaleRepository,
                             MerchantValidator merchantValidator,
                             Keycloak keycloakAdminClient,
                             @Value("${keycloak.admin.realm}") String realm,
                             PdndInfoCamereConnectorImpl pdndConnector,
                              InitiativeRestClient initiativeRestClient,
                              TransactionConnector transactionConnector) {
    this.merchantDetailService = merchantDetailService;
    this.merchantListService = merchantListService;
    this.merchantProcessOperationService = merchantProcessOperationService;
    this.merchantUpdatingInitiativeService = merchantUpdatingInitiativeService;
    this.merchantUpdateIbanService = merchantUpdateIbanService;
    this.merchantRepository = merchantRepository;
    this.uploadingMerchantService = uploadingMerchantService;
    this.initiative2InitiativeDTOMapper = initiative2InitiativeDTOMapper;
    this.merchantCreateDTOMapper = merchantCreateDTOMapper;
    this.pointOfSaleRepository = pointOfSaleRepository;
    this.merchantValidator = merchantValidator;
    this.keycloakAdminClient = keycloakAdminClient;
    this.realm = realm;
    this.pdndConnector = pdndConnector;
    this.initiativeRestClient = initiativeRestClient;
    this.transactionConnector = transactionConnector;
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
  public MerchantDetailDTO patchMerchant(String merchantId, String initiativeId,
      MerchantIbanPatchDTO merchantIbanPatchDTO) {
    return merchantUpdateIbanService.patchMerchant(merchantId, initiativeId,
        merchantIbanPatchDTO);
  }

  @Override
  public Merchant getMerchantByMerchantId(String merchantId) {
    return merchantRepository.findById(merchantId)
        .orElseThrow(() -> new MerchantNotFoundException(
            String.format("Merchant with id %s not found", merchantId)
        ));
  }

  @Override
  public MerchantListDTO getMerchantList(String initiativeId, Pageable pageable) {
    return merchantListService.getMerchantList(initiativeId, pageable);
  }

  @Override
  public MerchantRefundBatchHistoryDTO getMerchantRefundBatchesHistory(
      String merchantFiscalCodeOrVatNumber) {
    Merchant merchant = merchantRepository
        .findByFiscalCodeOrVatNumber(merchantFiscalCodeOrVatNumber, merchantFiscalCodeOrVatNumber)
        .orElseThrow(() -> new MerchantNotFoundException(
            String.format(MerchantConstants.ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE,
                merchantFiscalCodeOrVatNumber)));

    List<MerchantRefundBatchDTO> rewardBatches = Optional.ofNullable(merchant.getInitiativeList())
        .orElse(Collections.emptyList())
        .stream()
        .map(Initiative::getInitiativeId)
        .filter(Objects::nonNull)
        .distinct()
        .flatMap(initiativeId -> getAllRewardBatchesForInitiative(merchant.getMerchantId(), initiativeId)
            .stream())
        .sorted(Comparator.comparing(MerchantRefundBatchDTO::getMonth,
            Comparator.nullsLast(String::compareTo)).reversed())
        .toList();

    return MerchantRefundBatchHistoryDTO.builder()
        .merchantId(merchant.getMerchantId())
        .fiscalCode(merchant.getFiscalCode())
        .vatNumber(merchant.getVatNumber())
        .rewardBatches(rewardBatches)
        .build();
  }


  @Override
  public Page<InitiativeResponse> processMerchantInitiatives(
          String merchantId,
          String initiativeName,
          Pageable pageable) {

    Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new MerchantNotFoundException(merchantId));

    log.info("[AVAILABLE_INITIATIVES] Retrieving initiatives for merchant [{}]",
            sanitizeString(merchantId));

    List<String> newAtecoCodes = Optional.ofNullable(
                    pdndConnector.retrieveAtecoCodes(
                            merchant.getFiscalCode(),
                            merchant.getAtecoCodes()))
            .orElse(Collections.emptyList());

    Set<String> currentAtecoCodes = new HashSet<>(
            Optional.ofNullable(merchant.getAtecoCodes())
                    .orElse(Collections.emptyList()));

    Set<String> retrievedAtecoCodes = new HashSet<>(newAtecoCodes);

    log.info("[AVAILABLE_INITIATIVES] Retrieved {} ATECO codes from PDND for merchant [{}]",
            newAtecoCodes.size(),
            sanitizeString(merchantId));

    if (!retrievedAtecoCodes.equals(currentAtecoCodes)) {
      merchant.setAtecoCodes(newAtecoCodes);
      merchant.setUpdateDate(LocalDateTime.now(ZONEID));
      merchantRepository.save(merchant);

      log.info("[AVAILABLE_INITIATIVES] Updated ATECO codes for merchant [{}]",
              sanitizeString(merchantId));
    }

    Set<String> existingIds = Optional.ofNullable(merchant.getInitiativeList())
            .orElse(Collections.emptyList())
            .stream()
            .map(Initiative::getInitiativeId)
            .collect(Collectors.toSet());

    InitiativeSearchRequest request =
            new InitiativeSearchRequest(existingIds, newAtecoCodes, initiativeName);

    log.info(
            "[AVAILABLE_INITIATIVES] Searching initiatives for merchant [{}] (excluded initiatives: {}, initiativeName: {})",
            sanitizeString(merchantId),
            existingIds.size(),
            initiativeName != null ? sanitizeString(initiativeName) : "");

    PageResponse<InitiativeResponse> remoteResponse =
            initiativeRestClient.searchInitiatives(request, pageable).getBody();

    List<InitiativeResponse> content = remoteResponse != null
            ? remoteResponse.getContent()
            : Collections.emptyList();

    long totalElements = remoteResponse != null
            ? remoteResponse.getTotalElements()
            : 0L;

    log.info(
            "[AVAILABLE_INITIATIVES] Found {} initiatives for merchant [{}]",
            totalElements,
            sanitizeString(merchantId));

    return new PageImpl<>(content, pageable, totalElements);
  }

    @Override
  public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
    Optional<Merchant> merchant = merchantRepository.findById(merchantId);

    return merchant.map(value -> Optional.ofNullable(value.getInitiativeList()).orElse(Collections.emptyList()).stream()
        .filter(i -> MerchantConstants.INITIATIVE_PUBLISHED.equals(i.getStatus()))
        .sorted(Comparator.comparing(Initiative::getInitiativeName))
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
    merchant.setUpdateDate(LocalDateTime.now(ZONEID));
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
      existingMerchant.setLastLogin(LocalDateTime.now(ZONEID));
      merchantRepository.save(existingMerchant);
      log.info("[UPDATE_MERCHANT] Merchant with merchantId={} successfully updated", existingMerchant.getMerchantId());
      return existingMerchant.getMerchantId();

    }else {
      String merchantId = createNewMerchant(merchantCreateDTO);
      log.info("[CREATE_MERCHANT] Merchant with merchantId={} successfully created", merchantId);
      return merchantId;
    }
  }

  /**
   * Verifies if the merchant exists in the system.
   *
   * @param merchantId the ID of the merchant to check
   * @throws MerchantNotFoundException if the merchant does not exist
   */
  @Override
  public void verifyMerchantExists(String merchantId) {
    MerchantDetailDTO merchantDetail = getMerchantDetail(merchantId);
    if (merchantDetail == null) {
      throw new MerchantNotFoundException(
              String.format(MerchantConstants.ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE, merchantId));
    }
  }

  private List<MerchantRefundBatchDTO> getAllRewardBatchesForInitiative(String merchantId,
      String initiativeId) {
    int page = 0;
    boolean hasNext = true;
    List<MerchantRefundBatchDTO> result = new ArrayList<>();

    while (hasNext) {
      MerchantRewardBatchListDTO response = transactionConnector.getRewardBatches(
          merchantId,
          initiativeId,
          PageRequest.of(page, REWARD_BATCH_PAGE_SIZE)
      );

      if (response == null || response.getContent() == null || response.getContent().isEmpty()) {
        hasNext = false;
      } else {
        response.getContent().forEach(batch -> result.add(MerchantRefundBatchDTO.builder()
            .rewardBatchId(batch.getId())
            .initiativeId(batch.getInitiativeId())
            .month(batch.getMonth())
            .status(batch.getStatus())
            .approvedAmountCents(batch.getApprovedAmountCents())
            .suspendedAmountCents(batch.getSuspendedAmountCents())
            .initialAmountCents(batch.getInitialAmountCents())
            .currentAmountCents(batch.getCurrentAmountCents())
            .excludedAmountCents(batch.getExcludedAmountCents())
            .numberOfTransactions(batch.getNumberOfTransactions())
            .numberOfTransactionsSuspended(batch.getNumberOfTransactionsSuspended())
            .numberOfTransactionsRejected(batch.getNumberOfTransactionsRejected())
            .numberOfTransactionsElaborated(batch.getNumberOfTransactionsElaborated())
            .build()));
        page++;
        hasNext = page < response.getTotalPages();
      }
    }

    return result;
  }

  private void deleteKeycloakUsers(List<PointOfSale> pointsOfSale) {
    UsersResource usersResource = keycloakAdminClient.realm(realm).users();

    for (PointOfSale pos : pointsOfSale) {
      String email = pos.getContactEmail();
      if (email != null && !email.isEmpty()) {
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
  }

  private void updateMerchant(Merchant existingMerchant, MerchantCreateDTO merchantCreateDTO) {

    boolean updated = false;

    if (StringUtils.isNotBlank(merchantCreateDTO.getIban())) {
      existingMerchant.setIban(merchantCreateDTO.getIban());
      updated = true;
    }

    if (StringUtils.isNotBlank(merchantCreateDTO.getBusinessName())) {
      existingMerchant.setBusinessName(merchantCreateDTO.getBusinessName());
      updated = true;
    }

    if (StringUtils.isNotBlank(merchantCreateDTO.getIbanHolder())) {
      existingMerchant.setIbanHolder(merchantCreateDTO.getIbanHolder());
      updated = true;
    }

    if (merchantCreateDTO.getActivationDate() != null) {
      existingMerchant.setActivationDate(merchantCreateDTO.getActivationDate());
      updated = true;
    }

    if (updated) {
      existingMerchant.setUpdateDate(LocalDateTime.now(ZONEID));
    }
  }

  private String createNewMerchant(MerchantCreateDTO merchantCreateDTO) {
    String merchantId = Utilities.toUUID(merchantCreateDTO.getFiscalCode().concat("_").concat(merchantCreateDTO.getAcquirerId()));
    List<Initiative> initiatives = new ArrayList<>();

    Merchant merchant = merchantCreateDTOMapper.dtoToEntity(merchantCreateDTO, merchantId);
    merchant.setInitiativeList(initiatives);
    merchant.setEnabled(true);
    merchant.setLastLogin(LocalDateTime.now(ZONEID));
    merchant.setUpdateDate(LocalDateTime.now(ZONEID));
    merchant.setCreatedAt(LocalDateTime.now(ZONEID));
    merchantRepository.save(merchant);
    return merchantId;
  }




}