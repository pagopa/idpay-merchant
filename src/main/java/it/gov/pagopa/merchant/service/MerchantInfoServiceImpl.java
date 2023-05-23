package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantInfoDTO;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MerchantInfoServiceImpl implements MerchantInfoService {
    private final MerchantRepository merchantRepository;

    public MerchantInfoServiceImpl(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public MerchantInfoDTO getMerchantInfo(String fiscalCode, String acquirerId) {
        String merchantId = merchantRepository.findByFiscalCodeAndAcquirerId(fiscalCode, acquirerId)
                .orElseThrow(() -> new ClientExceptionWithBody(
                        HttpStatus.NOT_FOUND,
                        MerchantConstants.NOT_FOUND,
                        String.format(MerchantConstants.INITIATIVE_AND_MERCHANT_NOT_FOUND, fiscalCode, acquirerId
                ))).getMerchantId();

        return new MerchantInfoDTO(merchantId);
    }
}
