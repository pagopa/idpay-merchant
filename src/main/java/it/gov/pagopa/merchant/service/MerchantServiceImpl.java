package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.merchant.MerchantListService;
import it.gov.pagopa.merchant.service.merchant.UploadingMerchantService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class MerchantServiceImpl implements MerchantService{

    private final UploadingMerchantService uploadingMerchantService;
    private final MerchantDetailService merchantDetailService;
    private final MerchantListService merchantListService;
    private final MerchantRepository merchantRepository;

    public MerchantServiceImpl(
            MerchantDetailService merchantDetailService,
            MerchantListService merchantListService,
            MerchantRepository merchantRepository) {
            UploadingMerchantService uploadingMerchantService, MerchantDetailService merchantDetailService,) {
        this.uploadingMerchantService = uploadingMerchantService;
        this.merchantDetailService = merchantDetailService;
        this.merchantListService = merchantListService;
        this.merchantRepository = merchantRepository;
    }

    @Override
    public MerchantUpdateDTO uploadMerchantFile(MultipartFile file,
                                                String organizationId,
                                                String initiativeId,
                                                String organizationUserId){
        return uploadingMerchantService.uploadMerchantFile(file, organizationId, initiativeId, organizationUserId);
    }

    @Override
    public MerchantDetailDTO getMerchantDetail(String organizationId,
                                               String initiativeId,
                                               String merchantId) {
        return merchantDetailService.getMerchantDetail(organizationId, initiativeId, merchantId);
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
        return merchantRepository.findByAcquirerIdAndFiscalCode(acquirerId, fiscalCode)
                .map(Merchant::getMerchantId)
                .orElse(null);
    }
}
