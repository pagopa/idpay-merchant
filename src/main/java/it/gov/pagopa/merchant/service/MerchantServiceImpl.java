package it.gov.pagopa.merchant.service;

import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService {
    private final RetrieveMerchantIdService merchantIdService;

    public MerchantServiceImpl(RetrieveMerchantIdService merchantIdService) {
        this.merchantIdService = merchantIdService;
    }

    @Override
    public String retrieveMerchantId(String fiscalCode, String acquirerId) {
        return merchantIdService.getByFiscalCodeAndAcquirerId(fiscalCode, acquirerId);
    }
}
