package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantInfoDTO;

public interface MerchantInfoService {
    MerchantInfoDTO getMerchantInfo(String fiscalCode, String acquirerId);
}
