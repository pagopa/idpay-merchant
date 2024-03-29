package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.*;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.service.merchant.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MerchantServiceImpl implements MerchantService{

    private final MerchantDetailService merchantDetailService;
    private final MerchantListService merchantListService;
    private final MerchantProcessOperationService merchantProcessOperationService;
    private final MerchantUpdatingInitiativeService merchantUpdatingInitiativeService;
    private final MerchantRepository merchantRepository;
    private final UploadingMerchantService uploadingMerchantService;
    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper;

    public MerchantServiceImpl(
            MerchantDetailService merchantDetailService,
            MerchantListService merchantListService,
            MerchantProcessOperationService merchantProcessOperationService, MerchantUpdatingInitiativeService merchantUpdatingInitiativeService, MerchantRepository merchantRepository,
            UploadingMerchantService uploadingMerchantService,
            Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper) {
        this.merchantDetailService = merchantDetailService;
        this.merchantListService = merchantListService;
        this.merchantProcessOperationService = merchantProcessOperationService;
        this.merchantUpdatingInitiativeService = merchantUpdatingInitiativeService;
        this.merchantRepository = merchantRepository;
        this.uploadingMerchantService = uploadingMerchantService;
        this.initiative2InitiativeDTOMapper = initiative2InitiativeDTOMapper;
    }

    @Override
    public MerchantUpdateDTO uploadMerchantFile(MultipartFile file,
                                                String organizationId,
                                                String initiativeId,
                                                String organizationUserId,
                                                String acquirerId){
        return uploadingMerchantService.uploadMerchantFile(file, organizationId, initiativeId, organizationUserId, acquirerId);
    }

    @Override
    public MerchantDetailDTO getMerchantDetail(String organizationId,
                                               String initiativeId,
                                               String merchantId) {
        return merchantDetailService.getMerchantDetail(organizationId, initiativeId, merchantId);
    }

    @Override
    public MerchantDetailDTO getMerchantDetail(String merchantId, String initiativeId) {
        return merchantDetailService.getMerchantDetail(merchantId, initiativeId);
    }

    @Override
    public MerchantListDTO getMerchantList(String organizationId,
                                           String initiativeId,
                                           String fiscalCode,
                                           Pageable pageable) {
        return merchantListService.getMerchantList(organizationId, initiativeId, fiscalCode, pageable);
    }
    @Override
    public String retrieveMerchantId(String acquirerId, String fiscalCode) {
        return merchantRepository.retrieveByAcquirerIdAndFiscalCode(acquirerId, fiscalCode)
                .map(Merchant::getMerchantId)
                .orElse(null);
    }

    @Override
    public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
        Optional<Merchant> merchant = merchantRepository.findById(merchantId);

        return merchant.map(value -> value.getInitiativeList().stream()
                .filter(i -> MerchantConstants.INITIATIVE_PUBLISHED.equals(i.getStatus()))
                .map(initiative2InitiativeDTOMapper::apply)
                .toList()).orElse(Collections.emptyList());
    }

    @Override
    public void processOperation(QueueCommandOperationDTO queueCommandOperationDTO) {
        merchantProcessOperationService.processOperation(queueCommandOperationDTO);
    }

    @Override
    public void updatingInitiative(QueueInitiativeDTO queueInitiativeDTO) {
        merchantUpdatingInitiativeService.updatingInitiative(queueInitiativeDTO);
    }
}
