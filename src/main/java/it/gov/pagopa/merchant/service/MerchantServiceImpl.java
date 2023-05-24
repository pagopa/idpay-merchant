package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
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


    public MerchantServiceImpl(
            UploadingMerchantService uploadingMerchantService, MerchantDetailService merchantDetailService,
            MerchantListService merchantListService) {
        this.uploadingMerchantService = uploadingMerchantService;
        this.merchantDetailService = merchantDetailService;
        this.merchantListService = merchantListService;
    }

    @Override
    public MerchantUpdateDTO uploadMerchantFile(
            MultipartFile file,
            String organizationId,
            String initiativeId){
        return uploadingMerchantService.uploadMerchantFile(file, organizationId, initiativeId);
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
}
