package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.model.Merchant;
import org.springframework.stereotype.Component;

@Component
public class MerchantModelToDTOMapper {

    public MerchantDetailDTO toMerchantDetailDTO(Merchant merchant, String initiativeId) {
        MerchantDetailDTO merchantDetailDTO = new MerchantDetailDTO();
        merchant.getInitiativeList().stream().filter(i -> i.getInitiativeId().equals(initiativeId)).findFirst().ifPresent(
                initiative -> {
                    merchantDetailDTO.setInitiativeId(initiativeId);
                    merchantDetailDTO.setInitiativeName(initiative.getInitiativeName());
                    merchantDetailDTO.setBusinessName(merchant.getBusinessName());
                    merchantDetailDTO.setLegalOfficeAddress(merchant.getLegalOfficeAddress());
                    merchantDetailDTO.setLegalOfficeMunicipality(merchant.getLegalOfficeMunicipality());
                    merchantDetailDTO.setLegalOfficeProvince(merchant.getLegalOfficeProvince());
                    merchantDetailDTO.setLegalOfficeZipCode(merchant.getLegalOfficeZipCode());
                    merchantDetailDTO.setCertifiedEmail(merchant.getCertifiedEmail());
                    merchantDetailDTO.setFiscalCode(merchant.getFiscalCode());
                    merchantDetailDTO.setVatNumber(merchant.getVatNumber());
                    merchantDetailDTO.setStatus(initiative.getMerchantStatus());
                    merchantDetailDTO.setIban(merchant.getIban());
                    merchantDetailDTO.setIbanHolder(merchant.getIbanHolder());
                    merchantDetailDTO.setCreationDate(initiative.getCreationDate());
                    merchantDetailDTO.setUpdateDate(initiative.getUpdateDate());
                }
        );

        return merchantDetailDTO;
    }

    public MerchantDetailDTO toMerchantDetailDTOWithoutInitiative(Merchant merchant) {
        MerchantDetailDTO merchantDetailDTOWithoutInitiative = new MerchantDetailDTO();
        merchantDetailDTOWithoutInitiative.setVatNumber(merchant.getVatNumber());
        merchantDetailDTOWithoutInitiative.setIban(merchant.getIban());
        return merchantDetailDTOWithoutInitiative;
    }
}
