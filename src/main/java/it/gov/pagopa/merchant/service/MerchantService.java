package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MerchantService {

    MerchantListDTO getMerchantList(String organizationId, String initiativeId, String fiscalCode, Pageable pageable);
    MerchantDetailDTO getMerchantDetail(String organizationId, String initiativeId, String merchantId);
    List<InitiativeDTO> getMerchantInitiativeList(String merchantId);
}
