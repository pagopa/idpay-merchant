package it.gov.pagopa.merchant.mapper;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.model.PointOfSale;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PointOfSaleDTOMapper {

    public PointOfSaleDTO pointOfSaleEntityToPointOfSaleDTO(PointOfSale pointOfSale){
        if(pointOfSale == null){
            return  null;
        }
        return PointOfSaleDTO.builder()
                .id(pointOfSale.getId().toString())
                .type(PointOfSaleTypeEnum.fromValue(pointOfSale.getType()))
                .franchiseName(pointOfSale.getFranchiseName())
                .contactEmail(pointOfSale.getContactEmail())
                .contactName(pointOfSale.getContactName())
                .contactSurname(pointOfSale.getContactSurname())
                .channelEmail(pointOfSale.getChannelEmail())
                .channelPhone(pointOfSale.getChannelPhone())
                .channelGeolink(pointOfSale.getChannelGeolink())
                .channelWebsite(pointOfSale.getWebsite())
                .region(pointOfSale.getRegion())
                .province(pointOfSale.getProvince())
                .city(pointOfSale.getCity())
                .zipCode(pointOfSale.getZipCode())
                .address(StringUtils.isEmpty(pointOfSale.getStreetNumber()) ? pointOfSale.getAddress() : pointOfSale.getAddress()+", "+pointOfSale.getStreetNumber())
                .website(pointOfSale.getWebsite())
                .build();
    }

    public PointOfSale pointOfSaleDTOtoPointOfSaleEntity(PointOfSaleDTO pointOfSaleDTO, String merchantId){
        if(pointOfSaleDTO == null || merchantId == null){
            return null;
        }
        PointOfSale pointOfSale = PointOfSale.builder()
                .id(StringUtils.isEmpty(pointOfSaleDTO.getId()) ? null : new ObjectId(pointOfSaleDTO.getId()))
                .type(pointOfSaleDTO.getType().name())
                .franchiseName(pointOfSaleDTO.getFranchiseName())
                .contactEmail(pointOfSaleDTO.getContactEmail())
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
            pointOfSale.setChannelWebsite(pointOfSaleDTO.getChannelWebsite());
            mapAddress(pointOfSaleDTO, pointOfSale);
        }
        else if(PointOfSaleTypeEnum.ONLINE.equals(pointOfSaleDTO.getType())){
            pointOfSale.setWebsite(pointOfSaleDTO.getWebsite());
        }

        return pointOfSale;
    }
    
    private void mapAddress(PointOfSaleDTO dto, PointOfSale entity) {
        String fullAddress = dto.getAddress();

        if (fullAddress != null && !fullAddress.isBlank()) {
            String trimmed = fullAddress.trim();

            String address = trimmed;
            String streetNumber = null;

            if (trimmed.contains(",")) {
                String[] parts = trimmed.split(",", 2);
                address = parts[0].trim();
                streetNumber = parts[1].trim();
            }
            else {
                Pattern pattern = Pattern.compile("^(.*?)\\s+(\\d+\\w*(?:/\\w*)?)$");
                Matcher matcher = pattern.matcher(trimmed);
                if (matcher.find()) {
                    address = matcher.group(1).trim();
                    streetNumber = matcher.group(2).trim();
                }
            }

            entity.setAddress(address);
            entity.setStreetNumber(streetNumber);
        }
    }

}
