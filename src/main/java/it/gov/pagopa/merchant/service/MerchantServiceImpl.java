package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.merchant.MerchantInitiativesService;
import it.gov.pagopa.merchant.service.merchant.MerchantListService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantServiceImpl implements MerchantService{

    private final MerchantDetailService merchantDetailService;
    private final MerchantListService merchantListService;
    private final MerchantInitiativesService merchantInitiativesService;

    public MerchantServiceImpl(
            MerchantDetailService merchantDetailService,
            MerchantListService merchantListService,
            MerchantInitiativesService merchantInitiativesService) {
        this.merchantDetailService = merchantDetailService;
        this.merchantListService = merchantListService;
        this.merchantInitiativesService = merchantInitiativesService;
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
    public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
        return merchantInitiativesService.getMerchantInitiativeList(merchantId);
    }
}
