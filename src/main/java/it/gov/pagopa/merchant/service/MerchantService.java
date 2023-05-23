package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import org.springframework.data.domain.Pageable;

public interface MerchantService {

    MerchantListDTO getMerchantList(String organizationId, String initiativeId, String fiscalCode, Pageable pageable);
    MerchantDetailDTO getMerchantDetail(String organizationId, String initiativeId, String merchantId);
    String retrieveMerchantId(String fiscalCode, String acquirerId);
}
