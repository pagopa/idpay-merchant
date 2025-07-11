package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.enums.ChannelTypeEnum;
import it.gov.pagopa.merchant.dto.enums.SaleTypeEnum;
import it.gov.pagopa.merchant.dto.sale.ChannelDTO;
import it.gov.pagopa.merchant.dto.sale.PointOfSaleDTO;
import it.gov.pagopa.merchant.model.Channel;
import it.gov.pagopa.merchant.model.PointOfSale;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class PointOfSaleDTOMapper {

    public PointOfSaleDTO PointOfSaleEntityToPointOfSaleDTO(PointOfSale pointOfSale){
        if(pointOfSale == null){
            return  null;
        }
        return PointOfSaleDTO.builder()
                .type(SaleTypeEnum.valueOf(pointOfSale.getSaleType()))
                .franchiseName(pointOfSale.getFranchiseName())
                .contactEmail(pointOfSale.getContactEmail())
                .contactName(pointOfSale.getContactName())
                .contactSurname(pointOfSale.getContactSurname())
                .channels(ChannelEntityToChannelDTO(pointOfSale.getChannels()))
                .region(pointOfSale.getRegion())
                .province(pointOfSale.getProvince())
                .city(pointOfSale.getCity())
                .zipCode(pointOfSale.getZipCode())
                .address(pointOfSale.getAddress())
                .streetNumber(pointOfSale.getStreetNumber())
                .website(pointOfSale.getWebsite())
                .build();
    }
    
    public PointOfSale PointOfSaleDTOtoPointOfSaleEntity(PointOfSaleDTO pointOfSaleDTO, String merchantId){
        if(pointOfSaleDTO == null){
            return null;
        }
        PointOfSale pointOfSale = PointOfSale.builder()
                .saleType(pointOfSaleDTO.getType().name())
                .franchiseName(pointOfSaleDTO.getFranchiseName())
                .contactEmail(pointOfSaleDTO.getContactEmail())
                .contactName(pointOfSaleDTO.getContactName())
                .contactSurname(pointOfSaleDTO.getContactSurname())
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .merchantId(merchantId)
                .build();

        if(SaleTypeEnum.FISICO.equals(pointOfSaleDTO.getType())){
            pointOfSale.setRegion(pointOfSaleDTO.getRegion());
            pointOfSale.setProvince(pointOfSaleDTO.getProvince());
            pointOfSale.setCity(pointOfSaleDTO.getCity());
            pointOfSale.setZipCode(pointOfSaleDTO.getZipCode());
            pointOfSale.setAddress(pointOfSaleDTO.getAddress());
            pointOfSale.setStreetNumber(pointOfSaleDTO.getStreetNumber());
            pointOfSale.setChannels(ChannelDTOtoChannelEntity(pointOfSaleDTO.getChannels()));
        }
        else if(SaleTypeEnum.ONLINE.equals(pointOfSaleDTO.getType())){
            pointOfSale.setWebsite(pointOfSale.getWebsite());
        }

        return pointOfSale;
    }

    private List<Channel> ChannelDTOtoChannelEntity(List<ChannelDTO> channelDTOS){
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

    private List<ChannelDTO> ChannelEntityToChannelDTO(List<Channel> channels){
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

}
