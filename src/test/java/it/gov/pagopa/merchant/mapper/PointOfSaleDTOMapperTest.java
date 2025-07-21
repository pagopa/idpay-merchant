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
                .build();


        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale);

        assertNotNull(result);
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
                .build();


        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale);

        assertNotNull(result);
    }

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_EntityIsNull(){
        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(null);

        assertNull(result);
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

        // Act
        PointOfSale actualPointOfSaleDTOtoPointOfSaleEntityResult = pointOfSaleDTOMapper
                .pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO, "42");

        // Assert
        verify(pointOfSaleDTO).getContactEmail();
        verify(pointOfSaleDTO).getContactName();
        verify(pointOfSaleDTO).getContactSurname();
        verify(pointOfSaleDTO).getFranchiseName();
        verify(pointOfSaleDTO, atLeast(1)).getType();
        assertEquals("ONLINE", actualPointOfSaleDTOtoPointOfSaleEntityResult.getType());
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
        when(pointOfSaleDTO.getAddress()).thenReturn("42 Main St");
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
    }
}
