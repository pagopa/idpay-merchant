package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.exception.custom.PosValidationException;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointOfSaleValidatorTest {

    private PointOfSaleValidator pointOfSaleValidator;

    @BeforeEach
    void setUp(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        pointOfSaleValidator = new PointOfSaleValidator(validator, 5);
    }

    @Test
    void testValidateViolationsPointOfSales_PointOfSaleListEmpty() {
        List<PointOfSaleDTO> emptyList = new ArrayList<>();

        ClientExceptionWithBody exception = assertThrows(ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validatePointOfSales(emptyList));

        assertEquals("Point of sales list cannot be empty.", exception.getMessage());
    }

    @Test
    void testValidateViolationsPointOfSales_PointOfSaleListIsNull() {
        ClientExceptionWithBody exception = assertThrows(ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validatePointOfSales(null));

        assertEquals("Point of sales list cannot be empty.", exception.getMessage());
    }

    @Test
    void testValidateViolationsPointOfSales_PointOfSaleListSizeExceed() {

        List<PointOfSaleDTO> pointOfSales = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            PointOfSaleDTO dto = PointOfSaleDTOFaker.mockInstance();
            pointOfSales.add(dto);
        }

        ClientExceptionWithBody exception = assertThrows(
                ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validatePointOfSales(pointOfSales)
        );

        assertEquals(PointOfSaleConstants.CODE_BAD_REQUEST, exception.getCode());
        assertEquals(PointOfSaleConstants.MSG_POINT_OF_SALE_SIZE_EXCEEDED.formatted(5), exception.getMessage());
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
        pointOfSalePhysical.setWebsite("https://www.google.it");
        pointOfSalePhysical.setChannelGeolink("https://maps.google.com/location");

        PointOfSaleDTO pointOfSaleOnline = PointOfSaleDTOFaker.mockInstance();
        pointOfSaleOnline.setType(PointOfSaleTypeEnum.ONLINE);
        pointOfSaleOnline.setContactEmail("email@email.it");
        pointOfSaleOnline.setChannelEmail("channel@email.it");
        pointOfSaleOnline.setWebsite("https://ShOp.ExAmPlE.co.uk/home");
        pointOfSaleOnline.setChannelGeolink("https://www.my-valid-channel.it/channel");

        List<PointOfSaleDTO> pointOfSaleDTOS = new ArrayList<>();
        pointOfSaleDTOS.add(pointOfSaleOnline);
        pointOfSaleDTOS.add(pointOfSalePhysical);

        assertDoesNotThrow(() -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));

        assertEquals("https://shop.example.co.uk/home", pointOfSaleOnline.getWebsite());
        assertEquals("https://www.my-valid-channel.it/channel", pointOfSaleOnline.getChannelGeolink());
    }

    @Test
    void validateViolationsPointOfSales_channelGeolinkIsNull_shouldBeOk() {
        PointOfSaleDTO pointOfSaleOnline = PointOfSaleDTOFaker.mockInstance();
        pointOfSaleOnline.setType(PointOfSaleTypeEnum.ONLINE);
        pointOfSaleOnline.setContactEmail("email@email.it");
        pointOfSaleOnline.setWebsite("https://www.google.it");

        pointOfSaleOnline.setChannelGeolink(null);

        List<PointOfSaleDTO> pointOfSaleDTOS = List.of(pointOfSaleOnline);

        assertDoesNotThrow(() -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));
        assertNull(pointOfSaleOnline.getChannelGeolink());
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

        PosValidationException exception = assertThrows(PosValidationException.class,
                () -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));

        assertEquals("Validation failed for one or more point of sales", exception.getMessage());
    }

    @Test
    void validateDuplicates_duplicateEmail_throwsPosValidationException() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setContactEmail("duplicate@email.it");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setContactEmail("duplicate@email.it");

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        PosValidationException exception = assertThrows(PosValidationException.class,
                () -> pointOfSaleValidator.validateViolationsPointOfSales(list));

        assertEquals("Validation failed for one or more point of sales", exception.getMessage());
    }

    @Test
    void validateDuplicates_blankEmailsNotConsideredDuplicates_ok() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setContactEmail("");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setContactEmail("  ");

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        assertDoesNotThrow(() -> pointOfSaleValidator.validatePointOfSales(list));
    }

    @Test
    void validateDuplicates_duplicatePhysicalPOS_throwsPosValidationException() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos1.setContactEmail("unique1@email.it");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos2.setContactEmail("unique2@email.it");
        pos2.setAddress(pos1.getAddress());
        pos2.setCity(pos1.getCity());
        pos2.setFranchiseName(pos1.getFranchiseName());

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        PosValidationException exception = assertThrows(PosValidationException.class,
                () -> pointOfSaleValidator.validateViolationsPointOfSales(list));

        assertEquals("Validation failed for one or more point of sales", exception.getMessage());
    }

    @Test
    void validateDuplicates_duplicatePhysicalPOSWithEquivalentFranchiseName_throwsPosValidationException() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos1.setContactEmail("unique1@email.it");
        pos1.setFranchiseName("trony spa");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos2.setContactEmail("unique2@email.it");
        pos2.setAddress(pos1.getAddress());
        pos2.setStreetNumber(pos1.getStreetNumber());
        pos2.setCity(pos1.getCity());
        pos2.setFranchiseName("  Trony   SPA  ");

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        PosValidationException exception = assertThrows(PosValidationException.class,
                () -> pointOfSaleValidator.validateViolationsPointOfSales(list));

        assertEquals("Validation failed for one or more point of sales", exception.getMessage());
    }

    @Test
    void validateDuplicates_physicalPOSSameAddressDifferentCity_ok() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos1.setContactEmail("unique1@email.it");
        pos1.setCity("Rome");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos2.setContactEmail("unique2@email.it");
        pos2.setAddress(pos1.getAddress());
        pos2.setFranchiseName(pos1.getFranchiseName());
        pos2.setCity("Milan");

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        assertDoesNotThrow(() -> pointOfSaleValidator.validatePointOfSales(list));
    }

    @Test
    void validateDuplicates_physicalPOSSameAddressDifferentStreetNumber_ok() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos1.setContactEmail("unique1@email.it");
        pos1.setCity("Rome");
        pos1.setStreetNumber("1");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos2.setContactEmail("unique2@email.it");
        pos2.setAddress(pos1.getAddress());
        pos2.setFranchiseName(pos1.getFranchiseName());
        pos2.setCity(pos1.getCity());

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        assertDoesNotThrow(() -> pointOfSaleValidator.validatePointOfSales(list));
    }

    @Test
    void validateDuplicates_physicalPOSSameAddressDifferentFranchise_ok() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos1.setContactEmail("unique1@email.it");
        pos1.setFranchiseName("FranchiseA");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setType(PointOfSaleTypeEnum.PHYSICAL);
        pos2.setContactEmail("unique2@email.it");
        pos2.setAddress(pos1.getAddress());
        pos2.setCity(pos1.getCity());
        pos2.setFranchiseName("FranchiseB");

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        assertDoesNotThrow(() -> pointOfSaleValidator.validatePointOfSales(list));
    }


    @Test
    void validateDuplicates_duplicateOnlinePOS_throwsPosValidationException() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setType(PointOfSaleTypeEnum.ONLINE);
        pos1.setContactEmail("unique1@email.it");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setType(PointOfSaleTypeEnum.ONLINE);
        pos2.setContactEmail("unique2@email.it");
        pos2.setWebsite(pos1.getWebsite());
        pos2.setFranchiseName(pos1.getFranchiseName());

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        PosValidationException exception = assertThrows(PosValidationException.class,
                () -> pointOfSaleValidator.validateViolationsPointOfSales(list));

        assertEquals("Validation failed for one or more point of sales", exception.getMessage());
    }

    @Test
    void validateDuplicates_onlinePOSSameWebsiteDifferentFranchise_ok() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setType(PointOfSaleTypeEnum.ONLINE);
        pos1.setContactEmail("unique1@email.it");
        pos1.setFranchiseName("FranchiseA");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setType(PointOfSaleTypeEnum.ONLINE);
        pos2.setContactEmail("unique2@email.it");
        pos2.setWebsite(pos1.getWebsite());
        pos2.setFranchiseName("FranchiseB");

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        assertDoesNotThrow(() -> pointOfSaleValidator.validatePointOfSales(list));
    }

    @Test
    void validateDuplicates_multipleDuplicateErrors_throwsPosValidationException() {
        PointOfSaleDTO pos1 = PointOfSaleDTOFaker.mockInstance();
        pos1.setType(PointOfSaleTypeEnum.ONLINE);
        pos1.setContactEmail("dup@email.it");

        PointOfSaleDTO pos2 = PointOfSaleDTOFaker.mockInstance();
        pos2.setType(PointOfSaleTypeEnum.ONLINE);
        pos2.setContactEmail("dup@email.it");
        pos2.setWebsite(pos1.getWebsite());
        pos2.setFranchiseName(pos1.getFranchiseName());

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(pos1, pos2));

        PosValidationException exception = assertThrows(PosValidationException.class,
                () -> pointOfSaleValidator.validateViolationsPointOfSales(list));

        assertEquals("Validation failed for one or more point of sales", exception.getMessage());
    }

    @Test
    void validateDuplicates_physicalAndOnlineDoNotCrossConflict_ok() {
        PointOfSaleDTO physical = PointOfSaleDTOFaker.mockInstance();
        physical.setType(PointOfSaleTypeEnum.PHYSICAL);
        physical.setContactEmail("unique1@email.it");

        PointOfSaleDTO online = PointOfSaleDTOFaker.mockInstance();
        online.setType(PointOfSaleTypeEnum.ONLINE);
        online.setContactEmail("unique2@email.it");
        online.setFranchiseName(physical.getFranchiseName());

        List<PointOfSaleDTO> list = new ArrayList<>(List.of(physical, online));

        assertDoesNotThrow(() -> pointOfSaleValidator.validatePointOfSales(list));
    }
}
