package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.InitiativeDTO;

import java.util.List;

public interface MerchantService {
    List<InitiativeDTO> getMerchantInitiativeList(String merchantId);
}
