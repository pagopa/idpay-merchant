package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ContextConfiguration(classes = {PointOfSaleDTOMapper.class})
@ExtendWith(SpringExtension.class)
class PointOfSaleDTOMapperTest {

    @Autowired
    private PointOfSaleDTOMapper pointOfSaleDTOMapper;

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_EntityIsNull(){
        PointOfSaleDTO result = pointOfSaleDTOMapper.entityToDto(null);

        assertNull(result);
    }

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_okPointOfSalePhysical(){
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();

        PointOfSaleDTO result = pointOfSaleDTOMapper.entityToDto(pointOfSale);

        assertNotNull(result);
    }

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_okPointOfSaleOnline(){
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
        pointOfSale.setType("ONLINE");

        PointOfSaleDTO result = pointOfSaleDTOMapper.entityToDto(pointOfSale);

        assertNotNull(result);
    }

    @Test
    void pointOfSaleDTOtoPointOfSaleEntity_isNull(){
        PointOfSale result = pointOfSaleDTOMapper.dtoToEntity(null,null);
        assertNull(result);
    }

    @Test
    void pointOfSaleDTOtoPointOfSaleEntity_isNull1(){
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
        PointOfSale result = pointOfSaleDTOMapper.dtoToEntity(pointOfSaleDTO,null);
        assertNull(result);
    }


    @Test
    void pointOfSaleDTOtoPointOfSaleEntity_ok(){
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
        PointOfSale result = pointOfSaleDTOMapper.dtoToEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setAddress("Via Giuseppe, 33");
        result = pointOfSaleDTOMapper.dtoToEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setAddress("Via Giuseppe 33");
        result = pointOfSaleDTOMapper.dtoToEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setAddress(null);
        result = pointOfSaleDTOMapper.dtoToEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setAddress("");
        result = pointOfSaleDTOMapper.dtoToEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setType(PointOfSaleTypeEnum.ONLINE);
        pointOfSaleDTO.setId(new ObjectId().toString());
        result = pointOfSaleDTOMapper.dtoToEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);
    }

    @Test
    void entityToDto_withMerchantIsNull() {
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();

        PointOfSaleDTO result = pointOfSaleDTOMapper.entityToDto(pointOfSale, null);

        assertNotNull(result);
        assertNull(result.getBusinessName());
        assertNull(result.getFiscalCode());
        assertNull(result.getVatNumber());
    }

    @Test
    void entityToDto_withMerchantNotNull() {
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();

        Merchant merchant = Merchant.builder()
            .businessName("Test Business")
            .fiscalCode("12345678901")
            .vatNumber("IT12345678901")
            .build();

        PointOfSaleDTO result = pointOfSaleDTOMapper.entityToDto(pointOfSale, merchant);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getType());

        assertEquals("Test Business", result.getBusinessName());
        assertEquals("12345678901", result.getFiscalCode());
        assertEquals("IT12345678901", result.getVatNumber());
    }

    @Test
    void entityToDto_pointOfSaleIsNull_returnsNull() {
        PointOfSaleDTO result = pointOfSaleDTOMapper.entityToDto(null, null);
        assertNull(result);
    }
}
