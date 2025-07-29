package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.common.web.exception.ValidationException;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(PointOfSaleValidatorTest.TestConfig.class)
class PointOfSaleValidatorTest {

    @Autowired
    private PointOfSaleValidator pointOfSaleValidator;


    @Test
    void testValidateViolationsPointOfSales_PointOfSaleListEmpty() {
        ClientExceptionWithBody exception = assertThrows(ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validatePointOfSales(List.of()));

        assertEquals("Point of sales list cannot be empty.", exception.getMessage());
    }

    @Test
    void testValidateViolationsPointOfSales_PointOfSaleListIsNull() {
        ClientExceptionWithBody exception = assertThrows(ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validatePointOfSales(null));

        assertEquals("Point of sales list cannot be empty.", exception.getMessage());
    }


    @Test
    void testValidateViolationsPointOfSales_OK() {
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
        assertDoesNotThrow(() -> pointOfSaleValidator.validatePointOfSales(List.of(pointOfSaleDTO)));
    }

    @Test
    void validateViolationsPointOfSales_pointOfSaleIsOk(){
        PointOfSaleDTO pointOfSalePhysical = PointOfSaleDTOFaker.mockInstance();
        pointOfSalePhysical.setType(PointOfSaleTypeEnum.PHYSICAL);
        PointOfSaleDTO pointOfSaleOnline = PointOfSaleDTOFaker.mockInstance();
        pointOfSaleOnline.setContactEmail("email@email.it");
        pointOfSaleOnline.setType(PointOfSaleTypeEnum.ONLINE);

        List<PointOfSaleDTO> pointOfSaleDTOS = new ArrayList<>();
        pointOfSaleDTOS.add(pointOfSaleOnline);
        pointOfSaleDTOS.add(pointOfSalePhysical);

        assertDoesNotThrow(() -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));

    }


    @Test
    void validateViolationsPointOfSales_validationFailForPointOfSales(){
        PointOfSaleDTO pointOfSalePhysical = PointOfSaleDTOFaker.mockInstance();
        pointOfSalePhysical.setType(PointOfSaleTypeEnum.PHYSICAL);
        pointOfSalePhysical.setAddress(null);
        PointOfSaleDTO pointOfSaleOnline = PointOfSaleDTOFaker.mockInstance();
        pointOfSaleOnline.setType(PointOfSaleTypeEnum.ONLINE);
        pointOfSaleOnline.setWebsite(null);

        List<PointOfSaleDTO> pointOfSaleDTOS = new ArrayList<>();
        pointOfSaleDTOS.add(pointOfSaleOnline);
        pointOfSaleDTOS.add(pointOfSalePhysical);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));

        assertEquals("Validation failed for one or more point of sales", exception.getMessage());
    }


    @Configuration
    static class TestConfig{
        @Bean
        public Validator validator(){
            return new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        }

        @Bean
        public PointOfSaleValidator pointOfSaleValidator(Validator validator){
            return new PointOfSaleValidator(validator);
        }
    }
}