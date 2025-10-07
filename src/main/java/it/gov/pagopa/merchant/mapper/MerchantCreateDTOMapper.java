package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.MerchantCreateDTO;
import it.gov.pagopa.merchant.model.Merchant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MerchantCreateDTOMapper {

  public Merchant dtoToEntity(MerchantCreateDTO dto, String merchantId){
    if(dto == null || StringUtils.isBlank(merchantId)){
      return null;
    }

    return Merchant.builder()
        .merchantId(merchantId)
        .fiscalCode(dto.getFiscalCode())
        .vatNumber(dto.getFiscalCode())
        .acquirerId(dto.getAcquirerId())
        .businessName(dto.getBusinessName())
        .iban(dto.getIban())
        .ibanHolder(dto.getIbanHolder())
        .activationDate(dto.getActivationDate())
        .build();
  }
}
