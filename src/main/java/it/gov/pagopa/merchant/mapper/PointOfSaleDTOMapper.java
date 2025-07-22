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
import java.util.regex.Pattern; // Import aggiunto per gestire errori di compilazione

@Component
public class PointOfSaleDTOMapper {

    // Compila il pattern regex una sola volta all'avvio dell'applicazione.
    // L'utilizzo di un Pattern static final è la pratica standard per prevenire ReDoS
    // e migliorare le performance, evitando ricompilazioni.
    // Il pattern è stato leggermente modificato per essere più specifico e ridurre il rischio di backtracking eccessivo.
    // Si usano quantificatori possessivi (es. `++`, `*+`) o atomici (`?>`) se la logica lo permette,
    // per prevenire backtracking eccessivo, ma spesso possono rendere il pattern meno flessibile.
    private static final Pattern ADDRESS_STREET_NUMBER_PATTERN = Pattern.compile("^(.*?)\\s*(\\d+[A-Za-z]?\\s*(?:[/-]?\\s*\\d*[A-Za-z])?)$");



    public PointOfSaleDTO pointOfSaleEntityToPointOfSaleDTO(PointOfSale pointOfSale){
        if(pointOfSale == null){
            return null;
        }
        return PointOfSaleDTO.builder()
                // Gestione dei null per ObjectId: toString() su un oggetto null genera NPE
                .id(pointOfSale.getId() != null ? pointOfSale.getId().toString() : null)
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
                // Metodo helper per costruire l'indirizzo completo, gestendo i null
                .address(pointOfSale.getAddress()+", "+pointOfSale.getStreetNumber())
                .website(pointOfSale.getWebsite())
                .build();
    }

    public PointOfSale pointOfSaleDTOtoPointOfSaleEntity(PointOfSaleDTO pointOfSaleDTO, String merchantId){
        // Usare StringUtils.isEmpty per merchantId è più robusto di merchantId == null
        if(pointOfSaleDTO == null || StringUtils.isEmpty(merchantId)){
            return null;
        }
        PointOfSale pointOfSale = PointOfSale.builder()
                // Validare la stringa ObjectId prima di crearne un oggetto
                // ObjectId.isValid() previene IllegalArgumentException per stringhe non valide
                .id(isValidObjectId(pointOfSaleDTO.getId()) ? new ObjectId(pointOfSaleDTO.getId()) : null)
                // Gestione di un possibile null per getType()
                .type(pointOfSaleDTO.getType() != null ? pointOfSaleDTO.getType().name() : null)
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
            return channelDTOS.stream()
                    // Filtra eventuali DTO null o DTO con tipo null prima della mappatura
                    .filter(dto -> dto != null && dto.getType() != null)
                    .map(dto ->
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
            return channels.stream()
                    // Filtra eventuali entità null o entità con tipo blank prima della mappatura
                    .filter(entity -> entity != null && StringUtils.isNotBlank(entity.getType()))
                    .map(entity ->
                            ChannelDTO.builder()
                                    // Gestire potenziali IllegalArgumentException se entity.getType() non è un ChannelTypeEnum valido
                                    .type(ChannelTypeEnum.valueOf(entity.getType()))
                                    .contact(entity.getContact())
                                    .build()
                    ).toList();
        }
    }



    private void mapAddress(PointOfSaleDTO dto, PointOfSale entity) {
        String fullAddress = dto.getAddress();
        if (StringUtils.isBlank(fullAddress)) {
            return;
        }

        String trimmed = fullAddress.trim();
        String address = trimmed;
        String streetNumber = null;

        if (trimmed.contains(",")) {
            String[] parts = trimmed.split(",", 2);
            address = parts[0].trim();
            streetNumber = parts[1].trim();
        } else {
            Matcher matcher = ADDRESS_STREET_NUMBER_PATTERN.matcher(trimmed);
            if (matcher.find()) {
                address = matcher.group(1).trim();
                streetNumber = matcher.groupCount() >= 2 ? matcher.group(2) : null;
                if (streetNumber != null) {
                    streetNumber = streetNumber.trim();
                }
            }
        }

        entity.setAddress(address);
        entity.setStreetNumber(streetNumber);
    }



    /**
     * Valida se una stringa è un ObjectId valido.
     * Previene IllegalArgumentException quando si tenta di creare ObjectId da una stringa non valida.
     */
    private boolean isValidObjectId(String id) {
        return !StringUtils.isEmpty(id) && ObjectId.isValid(id);
    }
}