package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.IbanPutDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;

public interface MerchantUpdateIbanService {

  MerchantDetailDTO updateIban(String merchantId, String organizationId, String initiativeId, IbanPutDTO ibanPutDTO);
}
