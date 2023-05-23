package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantInfoDTO;

public interface RetrieveMerchantIdService {
    MerchantInfoDTO getByFiscalCodeAndAcquirerId(String fiscalCode, String acquirerId);
}
