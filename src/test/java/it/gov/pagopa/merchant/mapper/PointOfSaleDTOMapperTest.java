package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ContextConfiguration(classes = {PointOfSaleDTOMapper.class})
@ExtendWith(SpringExtension.class)
class PointOfSaleDTOMapperTest {

    @Autowired
    private PointOfSaleDTOMapper pointOfSaleDTOMapper;

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_EntityIsNull(){
        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(null);

        assertNull(result);
    }

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_okPointOfSalePhysical(){
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();

        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale);

        assertNotNull(result);
    }

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_okButStreetNumberIsNull(){
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
        pointOfSale.setStreetNumber(null);

        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale);

        assertNotNull(result);
    }

    @Test
    void pointOfSaleEntityToPointOfSaleDTO_okPointOfSaleOnline(){
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
        pointOfSale.setType("ONLINE");

        PointOfSaleDTO result = pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale);

        assertNotNull(result);
    }

    @Test
    void pointOfSaleDTOtoPointOfSaleEntity_isNull(){
        PointOfSale result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(null,null);
        assertNull(result);
    }

    @Test
    void pointOfSaleDTOtoPointOfSaleEntity_isNull1(){
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
        PointOfSale result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,null);
        assertNull(result);
    }


    @Test
    void pointOfSaleDTOtoPointOfSaleEntity_ok(){
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
        PointOfSale result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setAddress("Via Giuseppe, 33");
        result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setAddress("Via Giuseppe 33");
        result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setAddress(null);
        result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setAddress("");
        result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);

        pointOfSaleDTO.setType(PointOfSaleTypeEnum.ONLINE);
        pointOfSaleDTO.setId(new ObjectId().toString());
        result = pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,"merchant-id");
        assertNotNull(result);
    }



}
