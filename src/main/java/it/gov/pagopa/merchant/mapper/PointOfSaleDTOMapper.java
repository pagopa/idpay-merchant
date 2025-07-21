package it.gov.pagopa.merchant.mapper;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.merchant.dto.enums.ChannelTypeEnum;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.ChannelDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.model.Channel;
import it.gov.pagopa.merchant.model.PointOfSale;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
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
                .channels(channelEntityToChannelDTO(pointOfSale.getChannels()))
                .region(pointOfSale.getRegion())
                .province(pointOfSale.getProvince())
                .city(pointOfSale.getCity())
                .zipCode(pointOfSale.getZipCode())
                .address(pointOfSale.getAddress()+", "+pointOfSale.getStreetNumber())
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
            mapAddress(pointOfSaleDTO, pointOfSale);
            pointOfSale.setChannels(channelDTOtoChannelEntity(pointOfSaleDTO.getChannels()));
        }
        else if(PointOfSaleTypeEnum.ONLINE.equals(pointOfSaleDTO.getType())){
            pointOfSale.setWebsite(pointOfSaleDTO.getWebsite());
        }

        return pointOfSale;
    }

    private List<Channel> channelDTOtoChannelEntity(List<ChannelDTO> channelDTOS){
        if (CollectionUtils.isEmpty(channelDTOS)) {
            return Collections.emptyList();
        } else {
            return channelDTOS.stream().map(dto ->
                    Channel.builder()
                            .type(dto.getType().name())
                            .contact(dto.getContact())
                            .build()
            ).toList();
        }
    }

    private List<ChannelDTO> channelEntityToChannelDTO(List<Channel> channels){
        if(CollectionUtils.isEmpty(channels)){
            return Collections.emptyList();
        }
        else{
            return channels.stream().map(entity ->
                    ChannelDTO.builder()
                            .type(ChannelTypeEnum.valueOf(entity.getType()))
                            .contact(entity.getContact())
                            .build()
            ).toList();
        }
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
