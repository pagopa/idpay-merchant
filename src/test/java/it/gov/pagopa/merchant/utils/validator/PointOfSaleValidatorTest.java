package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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


    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.example",
            "https://www.example/",
            "https://www.example/path",
            "https://WWW.example",
            "https://Www.Example/path",
            "  https://www.example  ",
            "https://www.it"
    })
    void validateViolationsPointOfSales_websiteWwwWithoutTopLevelDomain_ko(String website) {
        PointOfSaleDTO pointOfSale = buildValidOnlinePointOfSale();
        pointOfSale.setWebsite(website);

        List<PointOfSaleDTO> pointOfSaleDTOS = List.of(pointOfSale);

        PosValidationException exception = assertThrows(PosValidationException.class,
                () -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));

        assertSingleInvalidUrlError(exception, "website");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.example.it",
            "https://www.example.it/negozio",
            "https://www.example-shop.com",
            "https://WWW.EXAMPLE.IT/path",
            "https://www.shop.example.co.uk/home",
            "https://wwwexample.com",
            "https://www2.example.it",
            "https://example.it",
            "https://shop.example.it/www.example"
    })
    void validateViolationsPointOfSales_websiteValid_ok(String website) {
        PointOfSaleDTO pointOfSale = buildValidOnlinePointOfSale();
        pointOfSale.setWebsite(website);

        List<PointOfSaleDTO> pointOfSaleDTOS = List.of(pointOfSale);

        assertDoesNotThrow(() -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.example",
            "https://WWW.example/location"
    })
    void validateViolationsPointOfSales_channelGeolinkWwwWithoutTopLevelDomain_ko(String channelGeolink) {
        PointOfSaleDTO pointOfSale = buildValidOnlinePointOfSale();
        pointOfSale.setChannelGeolink(channelGeolink);

        List<PointOfSaleDTO> pointOfSaleDTOS = List.of(pointOfSale);

        PosValidationException exception = assertThrows(PosValidationException.class,
                () -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));

        assertSingleInvalidUrlError(exception, "channelGeolink");
    }

    @Test
    void validateViolationsPointOfSales_channelGeolinkWithWwwAndTopLevelDomain_ok() {
        PointOfSaleDTO pointOfSale = buildValidOnlinePointOfSale();
        pointOfSale.setChannelGeolink("https://www.google.com/maps/place/Roma");

        List<PointOfSaleDTO> pointOfSaleDTOS = List.of(pointOfSale);

        assertDoesNotThrow(() -> pointOfSaleValidator.validateViolationsPointOfSales(pointOfSaleDTOS));
    }

    private static PointOfSaleDTO buildValidOnlinePointOfSale() {
        PointOfSaleDTO pointOfSale = PointOfSaleDTOFaker.mockInstance();
        pointOfSale.setType(PointOfSaleTypeEnum.ONLINE);
        pointOfSale.setContactEmail("email@email.it");
        pointOfSale.setChannelEmail("channel@email.it");
        pointOfSale.setWebsite("https://www.example.it");
        pointOfSale.setChannelGeolink("https://maps.google.com/location");
        return pointOfSale;
    }

    private static void assertSingleInvalidUrlError(PosValidationException exception, String expectedField) {
        List<ValidationErrorDetail> errors = exception.getErrors();
        assertEquals(1, errors.size(), () -> "Unexpected validation errors: " + errors);
        ValidationErrorDetail error = errors.getFirst();
        assertEquals(expectedField, error.getField());
        assertEquals(PointOfSaleConstants.CODE_INVALID_URL, error.getCode());
        assertEquals(PointOfSaleConstants.MSG_INVALID_WEBSITE, error.getMessage());
        assertEquals(0, error.getIndex());
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

    @Test
    void validateEmailFormat_invalidEmail_throwsClientExceptionWithBody() {
        ClientExceptionWithBody exception = assertThrows(ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validateEmailFormat("referent@domain_with_underscore.it"));

        assertEquals(PointOfSaleConstants.CODE_INVALID_EMAIL, exception.getCode());
        assertEquals(PointOfSaleConstants.MSG_INVALID_EMAIL, exception.getMessage());
    }

    @Test
    void validateEmailFormat_missingDomainDot_throwsClientExceptionWithBody() {
        ClientExceptionWithBody exception = assertThrows(ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validateEmailFormat("acquavivadalila@gmailcomme"));

        assertEquals(PointOfSaleConstants.CODE_INVALID_EMAIL, exception.getCode());
        assertEquals(PointOfSaleConstants.MSG_INVALID_EMAIL, exception.getMessage());
    }

    @Test
    void validateEmailFormat_validLowercaseEmail_ok() {
        assertDoesNotThrow(() -> pointOfSaleValidator.validateEmailFormat("referent@example.it"));
    }

    @Test
    void validateEmailFormat_validEmailWithDomainDot_ok() {
        assertDoesNotThrow(() -> pointOfSaleValidator.validateEmailFormat("acquavivadalila@gmail.com"));
    }
}
