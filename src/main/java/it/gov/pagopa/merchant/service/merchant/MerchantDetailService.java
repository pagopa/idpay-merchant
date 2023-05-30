package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantDetailBaseDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;

public interface MerchantDetailService {
    MerchantDetailDTO getMerchantDetail(String organizationId, String initiativeId, String merchantId);
    MerchantDetailBaseDTO getMerchantDetail(String merchantId, String initiativeId);

}
