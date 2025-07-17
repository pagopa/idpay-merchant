package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantIbanPatchDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;

public interface MerchantUpdateIbanService {

  MerchantDetailDTO updateIban(String merchantId, String initiativeId, MerchantIbanPatchDTO merchantIbanPatchDTO);
}
