package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.enums.ChannelTypeEnum;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.ChannelDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO.PointOfSaleDTOBuilder;
import it.gov.pagopa.merchant.model.Channel;
import it.gov.pagopa.merchant.model.PointOfSale;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {PointOfSaleDTOMapper.class})
@ExtendWith(SpringExtension.class)
class PointOfSaleDTOMapperTest {

    @Autowired
    private PointOfSaleDTOMapper pointOfSaleDTOMapper;

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_channelsEmpty(){
        PointOfSale pointOfSale = PointOfSale.builder()
                .id(new ObjectId())
                .channels(List.of())
                .city("Oxford")
                .contactEmail("jane.doe@example.org")
                .contactName("Contact Name")
                .contactSurname("Doe")
                .franchiseName("Franchise Name")
                .province("Province")
                .region("us-east-2")
                .streetNumber("42")
                .type("PHYSICAL")
                .website("Website")
                .zipCode("21654")
                .address("Main St")
                .build();


        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale);

        assertNotNull(result);
        assertEquals(pointOfSale.getId().toString(), result.getId());
        assertEquals(PointOfSaleTypeEnum.PHYSICAL, result.getType());
        assertEquals("Main St, 42", result.getAddress());
        assertTrue(result.getChannels().isEmpty());
    }

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_ok(){
        PointOfSale pointOfSale = PointOfSale.builder()
                .id(new ObjectId())
                .channels(List.of(new Channel("MOBILE","00000")))
                .city("Oxford")
                .contactEmail("jane.doe@example.org")
                .contactName("Contact Name")
                .contactSurname("Doe")
                .franchiseName("Franchise Name")
                .province("Province")
                .region("us-east-2")
                .streetNumber("42")
                .type("PHYSICAL")
                .website("Website")
                .zipCode("21654")
                .address("Main St")
                .build();


        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale);

        assertNotNull(result);
        assertEquals(pointOfSale.getId().toString(), result.getId());
        assertEquals(PointOfSaleTypeEnum.PHYSICAL, result.getType());
        assertEquals("Main St, 42", result.getAddress());
        assertFalse(result.getChannels().isEmpty());
        assertEquals(1, result.getChannels().size());
        assertEquals(ChannelTypeEnum.MOBILE, result.getChannels().getFirst().getType());
        assertEquals("00000", result.getChannels().getFirst().getContact());
    }

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_EntityIsNull(){
        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(null);

        assertNull(result);
    }

    // Nuovo test per id PointOfSale nullo
    @Test
    void pointOfSaleEntityToPointOfSaleDTO_IdIsNull(){
        PointOfSale pointOfSale = PointOfSale.builder()
                .id(null) // ID nullo
                .channels(List.of())
                .city("Oxford")
                .contactEmail("jane.doe@example.org")
                .contactName("Contact Name")
                .contactSurname("Doe")
                .franchiseName("Franchise Name")
                .province("Province")
                .region("us-east-2")
                .streetNumber("42")
                .type("PHYSICAL")
                .website("Website")
                .zipCode("21654")
                .address("Main St")
                .build();

        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale);

        assertNotNull(result);
        assertNull(result.getId()); // Verifica che l'ID nel DTO sia nullo
    }

    // Nuovo test per streetNumber nullo o vuoto in PointOfSaleEntityToPointOfSaleDTO
    @Test
    void pointOfSaleEntityToPointOfSaleDTO_StreetNumberIsNullOrEmpty(){
        // Caso streetNumber null
        PointOfSale pointOfSaleNullStreetNumber = PointOfSale.builder()
                .id(new ObjectId())
                .address("Main St")
                .streetNumber(null)
                .type("PHYSICAL")
                .build();
        PointOfSaleDTO resultNullStreetNumber = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSaleNullStreetNumber);
        assertEquals("Main St, null", resultNullStreetNumber.getAddress()); // String "null" concatenata

        // Caso streetNumber vuoto
        PointOfSale pointOfSaleEmptyStreetNumber = PointOfSale.builder()
                .id(new ObjectId())
                .address("Main St")
                .streetNumber("")
                .type("PHYSICAL")
                .build();
        PointOfSaleDTO resultEmptyStreetNumber = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSaleEmptyStreetNumber);
        assertEquals("Main St, ", resultEmptyStreetNumber.getAddress()); // String vuota concatenata
    }


    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_dtoIsNull() {
        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(null,"merchant-id");

        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult);
    }

    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_MerchantIdIsNull() {
        PointOfSaleDTOBuilder builderResult = PointOfSaleDTO.builder();
        PointOfSaleDTO pointOfSaleDTO = builderResult.channels(new ArrayList<>())
                .city("Oxford")
                .contactEmail("jane.doe@example.org")
                .contactName("Contact Name")
                .contactSurname("Doe")
                .franchiseName("Franchise Name")
                .province("Province")
                .region("us-east-2")
                .type(PointOfSaleTypeEnum.PHYSICAL)
                .website("Website")
                .zipCode("21654")
                .build();

        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,null);

        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult);
    }

    // Test per il caso ONLINE con website popolato
    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_thenReturnWebsiteIsPopulated() {
        // Arrange
        PointOfSaleDTOBuilder builderResult = PointOfSaleDTO.builder();
        PointOfSaleDTO pointOfSaleDTO = builderResult.channels(new ArrayList<>())
                .city("Oxford")
                .contactEmail("jane.doe@example.org")
                .contactName("Contact Name")
                .contactSurname("Doe")
                .franchiseName("Franchise Name")
                .province("Province")
                .region("us-east-2")
                .type(PointOfSaleTypeEnum.ONLINE)
                .website("https://www.example.com")
                .zipCode("21654")
                .build();

        // Act
        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper
                .pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "42");

        // Assert
        assertEquals("ONLINE", actualPointOfSaleDTOtoPointOfSaleEntityResult.getType());
        assertEquals("https://www.example.com", actualPointOfSaleDTOtoPointOfSaleEntityResult.getWebsite());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getAddress()); // Assicurati che i campi fisici siano null
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getStreetNumber());
    }


    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_thenReturnAddressIsNull() {
        // Arrange
        PointOfSaleDTOBuilder builderResult = PointOfSaleDTO.builder();
        PointOfSaleDTO pointOfSaleDTO = builderResult.channels(new ArrayList<>())
                .city("Oxford")
                .contactEmail("jane.doe@example.org")
                .contactName("Contact Name")
                .contactSurname("Doe")
                .franchiseName("Franchise Name")
                .province("Province")
                .region("us-east-2")
                .type(PointOfSaleTypeEnum.ONLINE)
                .website("Website")
                .zipCode("21654")
                .build();

        // Act
        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper
                .pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "42");

        // Assert
        assertEquals("ONLINE", actualPointOfSaleDTOtoPointOfSaleEntityResult.getType());
    }

    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_givenArrayList_thenReturnZipCodeIs21654() {
        // Arrange
        PointOfSaleDTO pointOfSaleDTO = mock(PointOfSaleDTO.class);
        when(pointOfSaleDTO.getAddress()).thenReturn("Main St, 42");
        when(pointOfSaleDTO.getCity()).thenReturn("Oxford");
        when(pointOfSaleDTO.getContactEmail()).thenReturn("jane.doe@example.org");
        when(pointOfSaleDTO.getContactName()).thenReturn("Contact Name");
        when(pointOfSaleDTO.getContactSurname()).thenReturn("Doe");
        when(pointOfSaleDTO.getFranchiseName()).thenReturn("Franchise Name");
        when(pointOfSaleDTO.getProvince()).thenReturn("Province");
        when(pointOfSaleDTO.getRegion()).thenReturn("us-east-2");
        when(pointOfSaleDTO.getZipCode()).thenReturn("21654");
        when(pointOfSaleDTO.getChannels()).thenReturn(new ArrayList<>());
        when(pointOfSaleDTO.getType()).thenReturn(PointOfSaleTypeEnum.PHYSICAL);

        // Act
        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper
                .pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "42");

        // Assert
        verify(pointOfSaleDTO).getAddress();
        verify(pointOfSaleDTO).getChannels();
        verify(pointOfSaleDTO).getCity();
        verify(pointOfSaleDTO).getContactEmail();
        verify(pointOfSaleDTO).getContactName();
        verify(pointOfSaleDTO).getContactSurname();
        verify(pointOfSaleDTO).getFranchiseName();
        verify(pointOfSaleDTO).getProvince();
        verify(pointOfSaleDTO).getRegion();
        verify(pointOfSaleDTO, atLeast(1)).getType();
        verify(pointOfSaleDTO).getZipCode();
        assertEquals("21654", actualPointOfSaleDTOtoPointOfSaleEntityResult.getZipCode());
        assertEquals("Main St", actualPointOfSaleDTOtoPointOfSaleEntityResult.getAddress());
        assertEquals("42", actualPointOfSaleDTOtoPointOfSaleEntityResult.getStreetNumber()); // Verifica numero civico da virgola
        assertEquals("PHYSICAL", actualPointOfSaleDTOtoPointOfSaleEntityResult.getType());
        assertEquals("Oxford", actualPointOfSaleDTOtoPointOfSaleEntityResult.getCity());
        assertEquals("Province", actualPointOfSaleDTOtoPointOfSaleEntityResult.getProvince());
        assertEquals("us-east-2", actualPointOfSaleDTOtoPointOfSaleEntityResult.getRegion());
        assertTrue(actualPointOfSaleDTOtoPointOfSaleEntityResult.getChannels().isEmpty());
    }

    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_givenOnline() {
        // Arrange
        PointOfSaleDTO pointOfSaleDTO = mock(PointOfSaleDTO.class);
        when(pointOfSaleDTO.getContactEmail()).thenReturn("jane.doe@example.org");
        when(pointOfSaleDTO.getContactName()).thenReturn("Contact Name");
        when(pointOfSaleDTO.getContactSurname()).thenReturn("Doe");
        when(pointOfSaleDTO.getFranchiseName()).thenReturn("Franchise Name");
        when(pointOfSaleDTO.getType()).thenReturn(PointOfSaleTypeEnum.ONLINE);
        when(pointOfSaleDTO.getWebsite()).thenReturn("https://online.example.com");


        // Act
        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper
                .pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "42");

        // Assert
        verify(pointOfSaleDTO).getContactEmail();
        verify(pointOfSaleDTO).getContactName();
        verify(pointOfSaleDTO).getContactSurname();
        verify(pointOfSaleDTO).getFranchiseName();
        verify(pointOfSaleDTO, atLeast(1)).getType();
        verify(pointOfSaleDTO).getWebsite();
        assertEquals("ONLINE", actualPointOfSaleDTOtoPointOfSaleEntityResult.getType());
        assertEquals("https://online.example.com", actualPointOfSaleDTOtoPointOfSaleEntityResult.getWebsite());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getCity());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getProvince());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getRegion());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getStreetNumber());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getZipCode());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getChannels());
    }

    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_givenPointOfSaleDTOMapper() {
        // Arrange
        PointOfSaleDTOBuilder builderResult = PointOfSaleDTO.builder();
        PointOfSaleDTO pointOfSaleDTO = builderResult.channels(new ArrayList<>())
                .id(new ObjectId().toString())
                .city("Oxford")
                .contactEmail("jane.doe@example.org")
                .contactName("Contact Name")
                .contactSurname("Doe")
                .franchiseName("Franchise Name")
                .province("Province")
                .region("us-east-2")
                .type(PointOfSaleTypeEnum.ONLINE)
                .website("Website")
                .zipCode("21654")
                .build();

        // Act
        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper
                .pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "42");

        // Assert
        assertEquals("ONLINE", actualPointOfSaleDTOtoPointOfSaleEntityResult.getType());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getCity());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getProvince());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getRegion());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getStreetNumber());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getZipCode());
        assertNull(actualPointOfSaleDTOtoPointOfSaleEntityResult.getChannels());
    }


    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_thenReturnChannelsSizeIsTwo() {
        // Arrange
        ArrayList<ChannelDTO> channelDTOList = new ArrayList<>();
        ChannelDTO buildResult = ChannelDTO.builder().contact("Contact").type(ChannelTypeEnum.WEB).build();
        channelDTOList.add(buildResult);
        ChannelDTO buildResult2 = ChannelDTO.builder().contact("Contact").type(ChannelTypeEnum.WEB).build();
        channelDTOList.add(buildResult2);
        PointOfSaleDTO pointOfSaleDTO = mock(PointOfSaleDTO.class);
        when(pointOfSaleDTO.getAddress()).thenReturn("Main St 42"); // Indirizzo senza virgola per testare regex
        when(pointOfSaleDTO.getCity()).thenReturn("Oxford");
        when(pointOfSaleDTO.getContactEmail()).thenReturn("jane.doe@example.org");
        when(pointOfSaleDTO.getContactName()).thenReturn("Contact Name");
        when(pointOfSaleDTO.getContactSurname()).thenReturn("Doe");
        when(pointOfSaleDTO.getFranchiseName()).thenReturn("Franchise Name");
        when(pointOfSaleDTO.getProvince()).thenReturn("Province");
        when(pointOfSaleDTO.getRegion()).thenReturn("us-east-2");
        when(pointOfSaleDTO.getZipCode()).thenReturn("21654");
        when(pointOfSaleDTO.getChannels()).thenReturn(channelDTOList);
        when(pointOfSaleDTO.getType()).thenReturn(PointOfSaleTypeEnum.PHYSICAL);

        // Act
        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper
                .pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "42");

        // Assert
        verify(pointOfSaleDTO).getAddress();
        verify(pointOfSaleDTO).getChannels();
        verify(pointOfSaleDTO).getCity();
        verify(pointOfSaleDTO).getContactEmail();
        verify(pointOfSaleDTO).getContactName();
        verify(pointOfSaleDTO).getContactSurname();
        verify(pointOfSaleDTO).getFranchiseName();
        verify(pointOfSaleDTO).getProvince();
        verify(pointOfSaleDTO).getRegion();
        verify(pointOfSaleDTO, atLeast(1)).getType();
        verify(pointOfSaleDTO).getZipCode();
        List<Channel> channels = actualPointOfSaleDTOtoPointOfSaleEntityResult.getChannels();
        assertEquals(2, channels.size());
        Channel getResult = channels.get(0);
        assertEquals("Contact", getResult.getContact());
        Channel getResult2 = channels.get(1);
        assertEquals("Contact", getResult2.getContact());
        assertEquals("WEB", getResult.getType());
        assertEquals("WEB", getResult2.getType());
        assertEquals("Main St", actualPointOfSaleDTOtoPointOfSaleEntityResult.getAddress()); // Verifica indirizzo dalla regex
        assertEquals("42", actualPointOfSaleDTOtoPointOfSaleEntityResult.getStreetNumber()); // Verifica numero civico dalla regex
    }

    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_thenReturnChannelsSizeIsTwo_Other() {
        // Arrange
        ArrayList<ChannelDTO> channelDTOList = new ArrayList<>();
        ChannelDTO buildResult = ChannelDTO.builder().contact("Contact").type(ChannelTypeEnum.WEB).build();
        channelDTOList.add(buildResult);
        ChannelDTO buildResult2 = ChannelDTO.builder().contact("Contact").type(ChannelTypeEnum.WEB).build();
        channelDTOList.add(buildResult2);
        PointOfSaleDTO pointOfSaleDTO = mock(PointOfSaleDTO.class);
        when(pointOfSaleDTO.getAddress()).thenReturn("Street 66");
        when(pointOfSaleDTO.getCity()).thenReturn("Oxford");
        when(pointOfSaleDTO.getContactEmail()).thenReturn("jane.doe@example.org");
        when(pointOfSaleDTO.getContactName()).thenReturn("Contact Name");
        when(pointOfSaleDTO.getContactSurname()).thenReturn("Doe");
        when(pointOfSaleDTO.getFranchiseName()).thenReturn("Franchise Name");
        when(pointOfSaleDTO.getProvince()).thenReturn("Province");
        when(pointOfSaleDTO.getRegion()).thenReturn("us-east-2");
        when(pointOfSaleDTO.getZipCode()).thenReturn("21654");
        when(pointOfSaleDTO.getChannels()).thenReturn(channelDTOList);
        when(pointOfSaleDTO.getType()).thenReturn(PointOfSaleTypeEnum.PHYSICAL);

        // Act
        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper
                .pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "42");

        // Assert
        verify(pointOfSaleDTO).getAddress();
        verify(pointOfSaleDTO).getChannels();
        verify(pointOfSaleDTO).getCity();
        verify(pointOfSaleDTO).getContactEmail();
        verify(pointOfSaleDTO).getContactName();
        verify(pointOfSaleDTO).getContactSurname();
        verify(pointOfSaleDTO).getFranchiseName();
        verify(pointOfSaleDTO).getProvince();
        verify(pointOfSaleDTO).getRegion();
        verify(pointOfSaleDTO, atLeast(1)).getType();
        verify(pointOfSaleDTO).getZipCode();
        List<Channel> channels = actualPointOfSaleDTOtoPointOfSaleEntityResult.getChannels();
        assertEquals(2, channels.size());
        Channel getResult = channels.get(0);
        assertEquals("Contact", getResult.getContact());
        Channel getResult2 = channels.get(1);
        assertEquals("Contact", getResult2.getContact());
        assertEquals("WEB", getResult.getType());
        assertEquals("WEB", getResult2.getType());
        assertEquals("Street", actualPointOfSaleDTOtoPointOfSaleEntityResult.getAddress()); // Verifica indirizzo dalla regex
        assertEquals("66", actualPointOfSaleDTOtoPointOfSaleEntityResult.getStreetNumber()); // Verifica numero civico dalla regex
    }

    // Nuovo test: id DTO non valido per ObjectId
    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_IdDTOInvalid() {
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTO.builder()
                .id("invalidObjectIdString") // ID non valido
                .type(PointOfSaleTypeEnum.PHYSICAL)
                .build();

        PointOfSale result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "merchantId");

        assertNotNull(result);
        assertNull(result.getId()); // L'ID dovrebbe essere null perché non è valido
    }

    // Nuovo test: type DTO è null
    @Test
    void testPointOfSaleDTOtoPointOfSaleEntity_TypeDTONull() {
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTO.builder()
                .type(null) // Type nullo
                .build();

        PointOfSale result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "merchantId");

        assertNotNull(result);
        assertNull(result.getType()); // Il tipo dovrebbe essere null
    }

    // Nuovo test: channelDTOtoChannelEntity con lista di DTO null o con tipo null
    @Test
    void testChannelDTOtoChannelEntity_FilterNullOrInvalidChannels() {
        ChannelDTO validChannel = ChannelDTO.builder().type(ChannelTypeEnum.WEB).contact("web@example.com").build();
        ChannelDTO nullTypeChannel = ChannelDTO.builder().type(null).contact("nulltype@example.com").build();
        ChannelDTO nullChannel = null;

        List<ChannelDTO> mixedChannels = new ArrayList<>();
        mixedChannels.add(validChannel);
        mixedChannels.add(nullTypeChannel);
        mixedChannels.add(nullChannel);

        // Mock del mapper per accedere al metodo privato (non è il modo ideale, ma per coverage...)
        PointOfSaleDTOMapper spyMapper = spy(pointOfSaleDTOMapper);

        // Utilizziamo Reflection per accedere al metodo privato e testarlo direttamente
        try {
            java.lang.reflect.Method method = PointOfSaleDTOMapper.class.getDeclaredMethod("channelDTOtoChannelEntity", List.class);
            method.setAccessible(true);
            List<Channel> result = (List<Channel>) method.invoke(spyMapper, mixedChannels);

            assertNotNull(result);
            assertEquals(1, result.size()); // Solo il canale valido dovrebbe essere mappato
            assertEquals("WEB", result.getFirst().getType());
            assertEquals("web@example.com", result.getFirst().getContact());
        } catch (Exception e) {
            fail("Errore durante l'accesso al metodo privato: " + e.getMessage());
        }
    }


    // Nuovo test: channelEntityToChannelDTO con lista di entità null o con tipo blank
    @Test
    void testChannelEntityToChannelDTO_FilterNullOrBlankTypeChannels() {
        Channel validChannel = Channel.builder().type("WEB").contact("web@example.com").build();
        Channel blankTypeChannel = Channel.builder().type("   ").contact("blanktype@example.com").build();
        Channel nullChannel = null;

        List<Channel> mixedChannels = new ArrayList<>();
        mixedChannels.add(validChannel);
        mixedChannels.add(blankTypeChannel);
        mixedChannels.add(nullChannel);

        // Mock del mapper per accedere al metodo privato (non è il modo ideale, ma per coverage...)
        PointOfSaleDTOMapper spyMapper = spy(pointOfSaleDTOMapper);

        // Utilizziamo Reflection per accedere al metodo privato e testarlo direttamente
        try {
            java.lang.reflect.Method method = PointOfSaleDTOMapper.class.getDeclaredMethod("channelEntityToChannelDTO", List.class);
            method.setAccessible(true);
            List<ChannelDTO> result = (List<ChannelDTO>) method.invoke(spyMapper, mixedChannels);

            assertNotNull(result);
            assertEquals(1, result.size()); // Solo il canale valido dovrebbe essere mappato
            assertEquals(ChannelTypeEnum.WEB, result.getFirst().getType());
            assertEquals("web@example.com", result.getFirst().getContact());
        } catch (Exception e) {
            fail("Errore durante l'accesso al metodo privato: " + e.getMessage());
        }
    }

    // Nuovo test per mapAddress: indirizzo null o blank
    @Test
    void testMapAddress_NullOrBlankAddress() {
        PointOfSaleDTO dto = PointOfSaleDTO.builder().build();
        PointOfSale entity = new PointOfSale();

        // Caso null
        dto.setAddress(null);
        pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(dto, "merchantId"); // Chiamata che invoca mapAddress
        assertNull(entity.getAddress()); // Dovrebbero rimanere null se non inizializzati, o essere null se resettati
        assertNull(entity.getStreetNumber());

        // Caso blank
        dto.setAddress("   ");
        pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(dto, "merchantId"); // Chiamata che invoca mapAddress
        assertNull(entity.getAddress()); // Dovrebbero rimanere null se non inizializzati, o essere null se resettati
        assertNull(entity.getStreetNumber());
    }

    // Nuovo test per mapAddress: indirizzo senza numero civico finale (regex non match)
    @Test
    void testMapAddress_AddressWithoutStreetNumber() {
        PointOfSaleDTO dto = PointOfSaleDTO.builder()
                .address("Via Roma")
                .type(PointOfSaleTypeEnum.PHYSICAL)
                .build();
        PointOfSale entity = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(dto, "merchantId");

        assertEquals("Via Roma", entity.getAddress());
        assertNull(entity.getStreetNumber()); // Non dovrebbe trovare un numero civico
    }
}