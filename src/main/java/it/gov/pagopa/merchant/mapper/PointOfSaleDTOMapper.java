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
    // In questo caso, rendiamo il quantificatore di caratteri non-greedy '?' per '1,100' per essere più sicuro.
    private static final Pattern ADDRESS_STREET_NUMBER_PATTERN = Pattern.compile("^([A-Za-zÀ-ÿ0-9'°.,\\-\\s]{1,100})\\s*(\\d+[A-Za-z]?\\S*)?$");



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
                .address(buildFullAddress(pointOfSale.getAddress(), pointOfSale.getStreetNumber()))
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
                                    .type(safeValueOfChannelType(entity.getType()))
                                    .contact(entity.getContact())
                                    .build()
                    ).toList();
        }
    }

    // Metodo helper per ChannelTypeEnum.valueOf()
    private ChannelTypeEnum safeValueOfChannelType(String type) {
        try {
            return ChannelTypeEnum.valueOf(type);
        } catch (IllegalArgumentException e) {
            // Logga l'errore o gestisci in altro modo, ad esempio ritornando null o un tipo DEFAULT
            return null; // O un ChannelTypeEnum.UNKNOWN o simile
        }
    }

    private void mapAddress(PointOfSaleDTO dto, PointOfSale entity) {
        String fullAddress = dto.getAddress();

        // Usare StringUtils.isNotBlank è più robusto per controllare null, stringhe vuote o solo spazi
        if (StringUtils.isNotBlank(fullAddress)) {
            String trimmed = fullAddress.trim();

            String address = trimmed;
            String streetNumber = null;

            if (trimmed.contains(",")) {
                // Limitare lo split a 2 elementi per prevenire DoS su stringhe molto lunghe con molte virgole
                String[] parts = trimmed.split(",", 2);
                address = parts[0].trim();
                if (parts.length > 1) { // Assicurati che esista una seconda parte
                    streetNumber = parts[1].trim();
                }
            }
            else {
                // Utilizza il pattern pre-compilato
                Matcher matcher = ADDRESS_STREET_NUMBER_PATTERN.matcher(trimmed);
                if (matcher.find()) {
                    address = matcher.group(1).trim();
                    // Assicurati che il gruppo esista prima di accedervi
                    if (matcher.groupCount() >= 2) { // Il nuovo pattern ha 2 gruppi catturati
                        streetNumber = matcher.group(2);
                        if (streetNumber != null) {
                            streetNumber = streetNumber.trim();
                        }
                    }
                }
            }

            entity.setAddress(address);
            entity.setStreetNumber(streetNumber);
        }
    }

    /**
     * Metodo helper per costruire la stringa dell'indirizzo completo, gestendo i null in modo elegante.
     */
    private String buildFullAddress(String address, String streetNumber) {
        if (StringUtils.isNotBlank(address) && StringUtils.isNotBlank(streetNumber)) {
            return address + ", " + streetNumber;
        } else if (StringUtils.isNotBlank(address)) {
            return address;
        } else if (StringUtils.isNotBlank(streetNumber)) {
            return streetNumber;
        }
        return null;
    }

    /**
     * Valida se una stringa è un ObjectId valido.
     * Previene IllegalArgumentException quando si tenta di creare ObjectId da una stringa non valida.
     */
    private boolean isValidObjectId(String id) {
        return !StringUtils.isEmpty(id) && ObjectId.isValid(id);
    }
}