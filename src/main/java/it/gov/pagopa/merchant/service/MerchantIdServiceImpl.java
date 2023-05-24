package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MerchantIdServiceImpl implements MerchantIdService {
    private final MerchantRepository merchantRepository;

    public MerchantIdServiceImpl(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public String getMerchantInfo(String acquirerId, String fiscalCode) {
        return merchantRepository.findByFiscalCodeAndAcquirerId(acquirerId, fiscalCode)
                .orElseThrow(() -> new ClientExceptionWithBody(
                        HttpStatus.NOT_FOUND,
                        MerchantConstants.NOT_FOUND,
                        String.format(MerchantConstants.MERCHANTID_BY_ACQUIRERID_AND_FISCALCODE_MESSAGE, acquirerId, fiscalCode
                                ))).getMerchantId();
    }
}
