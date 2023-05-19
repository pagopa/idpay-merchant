package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.exception.MerchantException;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService {
    private final MerchantRepository merchantRepository;

    public MerchantServiceImpl(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public String retrieveMerchantId(String fiscalCode, String acquirerId) {
        return merchantRepository.findByFiscalCodeAndAcquirerId(fiscalCode, acquirerId)
                .orElseThrow(() -> new MerchantException(
                        MerchantConstants.Exception.NotFound.CODE,
                        String.format(MerchantConstants.Exception.NotFound.MERCHANTID_BY_FISCALCODE_AND_ACQUIRERID_MESSAGE, fiscalCode, acquirerId),
                        HttpStatus.NOT_FOUND
                )).getMerchantId();
    }
}
