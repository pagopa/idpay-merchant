package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.model.Merchant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class MerchantDTOToModelMapper {

    public Merchant toMerchant(MerchantDetailDTO dto, String acquirerId) {
        return Merchant.builder()
                .fiscalCode(dto.getFiscalCode())
                .businessName(dto.getBusinessName())
                .acquirerId(acquirerId)
                .vatNumber(dto.getVatNumber())
                .certifiedEmail(dto.getCertifiedEmail())
                .legalOfficeAddress(dto.getLegalOfficeAddress())
                .legalOfficeMunicipality(dto.getLegalOfficeMunicipality())
                .legalOfficeProvince(dto.getLegalOfficeProvince())
                .legalOfficeZipCode(dto.getLegalOfficeZipCode())
                .iban(dto.getIban())
                .ibanHolder(dto.getIbanHolder())
                .enabled(true)
                .build();
    }

    public Merchant buildMinmalMerchant(String merchantId, String acquirerId, String businessName, String fiscalCode) {
        return Merchant.builder()
                .merchantId(merchantId)
                .acquirerId(acquirerId)
                .businessName(businessName)
                .fiscalCode(fiscalCode)
                .initiativeList(new ArrayList<>())
                .enabled(true)
                .build();
        }
    }

