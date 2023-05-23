package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.merchant.MerchantListService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService{

    private final MerchantDetailService merchantDetailService;
    private final MerchantListService merchantListService;
    private final RetrieveMerchantIdService merchantIdService;

    public MerchantServiceImpl(
            MerchantDetailService merchantDetailService,
            MerchantListService merchantListService,
            RetrieveMerchantIdService merchantIdService) {
        this.merchantDetailService = merchantDetailService;
        this.merchantListService = merchantListService;
        this.merchantIdService = merchantIdService;
    }

    @Override
    public MerchantDetailDTO getMerchantDetail(String initiativeId,
                                               String merchantId) {
        return merchantDetailService.getMerchantDetail(initiativeId, merchantId);
    }
    @Override
    public MerchantListDTO getMerchantList(String initiativeId,
                                           String fiscalCode,
                                           Pageable pageable) {
        return merchantListService.getMerchantList(initiativeId, fiscalCode, pageable);
    }
    @Override
    public String retrieveMerchantId(String fiscalCode, String acquirerId) {
        return merchantIdService.getByFiscalCodeAndAcquirerId(fiscalCode, acquirerId);
    }
}
