package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantInfoDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.merchant.MerchantListService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService{

    private final MerchantDetailService merchantDetailService;
    private final MerchantListService merchantListService;
    private final MerchantInfoService merchantIdService;

    public MerchantServiceImpl(
            MerchantDetailService merchantDetailService,
            MerchantListService merchantListService,
            MerchantInfoService merchantIdService) {
        this.merchantDetailService = merchantDetailService;
        this.merchantListService = merchantListService;
        this.merchantIdService = merchantIdService;
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
    public MerchantInfoDTO retrieveMerchantId(String fiscalCode, String acquirerId) {
        return merchantIdService.getMerchantInfo(fiscalCode, acquirerId);
    }
}
