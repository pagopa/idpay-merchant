package it.gov.pagopa.merchant.mapper;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.model.PointOfSale;
import org.springframework.stereotype.Component;

@Component
public class PointOfSaleDTOMapper {

    public PointOfSaleDTO entityToDto(PointOfSale pointOfSale){
        if(pointOfSale == null){
            return  null;
        }
        return PointOfSaleDTO.builder()
                .id(pointOfSale.getId())
                .type(PointOfSaleTypeEnum.fromValue(pointOfSale.getType()))
                .franchiseName(pointOfSale.getFranchiseName())
                .contactEmail(pointOfSale.getContactEmail())
                .contactName(pointOfSale.getContactName())
                .contactSurname(pointOfSale.getContactSurname())
                .channelEmail(pointOfSale.getChannelEmail())
                .channelPhone(pointOfSale.getChannelPhone())
                .channelGeolink(pointOfSale.getChannelGeolink())
                .region(pointOfSale.getRegion())
                .province(pointOfSale.getProvince())
                .city(pointOfSale.getCity())
                .zipCode(pointOfSale.getZipCode())
                .address(pointOfSale.getAddress())
                .website(pointOfSale.getWebsite())
                .build();
    }

    public PointOfSale dtoToEntity(PointOfSaleDTO pointOfSaleDTO, String merchantId){
        if(pointOfSaleDTO == null || merchantId == null){
            return null;
        }
        PointOfSale pointOfSale = PointOfSale.builder()
                .id(StringUtils.isEmpty(pointOfSaleDTO.getId()) ? null : pointOfSaleDTO.getId())
                .type(pointOfSaleDTO.getType().name())
                .franchiseName(pointOfSaleDTO.getFranchiseName())
                .contactEmail(pointOfSaleDTO.getContactEmail())
                .website(pointOfSaleDTO.getWebsite())
                .contactName(pointOfSaleDTO.getContactName())
                .contactSurname(pointOfSaleDTO.getContactSurname())
                .merchantId(merchantId)
                .build();

        if(PointOfSaleTypeEnum.PHYSICAL.equals(pointOfSaleDTO.getType())){
            pointOfSale.setRegion(pointOfSaleDTO.getRegion());
            pointOfSale.setProvince(pointOfSaleDTO.getProvince());
            pointOfSale.setCity(pointOfSaleDTO.getCity());
            pointOfSale.setZipCode(pointOfSaleDTO.getZipCode());
            pointOfSale.setChannelEmail(pointOfSaleDTO.getChannelEmail());
            pointOfSale.setChannelPhone(pointOfSaleDTO.getChannelPhone());
            pointOfSale.setChannelGeolink(pointOfSaleDTO.getChannelGeolink());
            pointOfSale.setAddress(pointOfSaleDTO.getAddress());
        }

        return pointOfSale;
    }

}
