package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.InitiativeDTO;

import java.util.List;

public interface MerchantInitiativesService {
    List<InitiativeDTO> getMerchantInitiativeList(String merchantId);
}
