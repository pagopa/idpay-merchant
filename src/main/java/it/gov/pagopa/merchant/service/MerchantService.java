package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import org.springframework.data.domain.Pageable;

import it.gov.pagopa.merchant.dto.InitiativeDTO;

import java.util.List;

public interface MerchantService {

    MerchantListDTO getMerchantList(String initiativeId, String fiscalCode, Pageable pageable);
    MerchantDetailDTO getMerchantDetail(String initiativeId, String merchantId);
    List<InitiativeDTO> getMerchantInitiativeList(String merchantId);
}
