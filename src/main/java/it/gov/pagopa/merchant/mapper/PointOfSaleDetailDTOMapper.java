package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDetailDTO;
import org.springframework.stereotype.Component;

@Component
public class PointOfSaleDetailDTOMapper {

    public PointOfSaleDetailDTO pointOfSaleDetailDTO(PointOfSaleDTO pointOfSaleDTO, MerchantDetailDTO merchantDetailDTO) {
        PointOfSaleDTO minimalPOS = PointOfSaleDTO.builder()
                .id(pointOfSaleDTO.getId())
                .address(pointOfSaleDTO.getAddress())
                .channelPhone(pointOfSaleDTO.getChannelPhone())
                .contactEmail(pointOfSaleDTO.getContactEmail())
                .contactName(pointOfSaleDTO.getContactName())
                .contactSurname(pointOfSaleDTO.getContactSurname())
                .build();

        MerchantDetailDTO minimalMerchant = MerchantDetailDTO.builder()
                .vatNumber(merchantDetailDTO.getVatNumber())
                .iban(merchantDetailDTO.getIban())
                .build();

        return PointOfSaleDetailDTO.builder()
                .pointOfSale(minimalPOS)
                .merchant(minimalMerchant)
                .build();
    }
}
