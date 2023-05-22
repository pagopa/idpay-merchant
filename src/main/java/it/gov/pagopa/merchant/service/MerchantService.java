package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import org.springframework.data.domain.Pageable;

public interface MerchantService {

    MerchantListDTO getMerchantList(String initiativeId, String fiscalCode, Pageable pageable);
    MerchantDetailDTO getMerchantDetail(String initiativeId, String merchantId);
}
