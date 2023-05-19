package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;

public interface MerchantDetailService {
    MerchantDetailDTO getMerchantDetail(String initiativeId, String merchantId);

}
