package it.gov.pagopa.merchant.mapper;

import it.gov.pagopa.common.utils.TestUtils;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MerchantModelToDTOMapperTest {

 private MerchantModelToDTOMapper mapper;

 @BeforeEach
 void setUp() {
   mapper = new MerchantModelToDTOMapper();
 }

    @Test
    void merchantMapperTest(){
        Merchant merchant = MerchantFaker.mockInstance(1);
        merchant.setOperativeEmail("operative.email@test.com");

        Initiative initiative = merchant.getInitiativeList().get(0);

        MerchantDetailDTO result = mapper.toMerchantDetailDTO(merchant, initiative.getInitiativeId());

        assertAll(() -> {
            assertNotNull(result);
            TestUtils.checkNotNullFields(result);
            assertEquals(initiative.getInitiativeId(), result.getInitiativeId());
            assertEquals(initiative.getInitiativeName(), result.getInitiativeName());
            assertEquals(merchant.getBusinessName(), result.getBusinessName());
            assertEquals(merchant.getLegalOfficeAddress(), result.getLegalOfficeAddress());
            assertEquals(merchant.getLegalOfficeMunicipality(), result.getLegalOfficeMunicipality());
            assertEquals(merchant.getLegalOfficeProvince(), result.getLegalOfficeProvince());
            assertEquals(merchant.getLegalOfficeZipCode(), result.getLegalOfficeZipCode());
            assertEquals(merchant.getCertifiedEmail(), result.getCertifiedEmail());
            assertEquals(merchant.getOperativeEmail(), result.getOperativeEmail());
            assertEquals(merchant.getFiscalCode(), result.getFiscalCode());
            assertEquals(merchant.getVatNumber(), result.getVatNumber());
            assertEquals(initiative.getMerchantStatus(), result.getStatus());
            assertEquals(merchant.getIban(), result.getIban());
            assertEquals(merchant.getIbanHolder(), result.getIbanHolder());
            assertEquals(initiative.getCreationDate(), result.getCreationDate());
            assertEquals(initiative.getUpdateDate(), result.getUpdateDate());
            assertEquals(merchant.getActivationDate(), result.getActivationDate());
        });
    }

    @Test
    void merchantMapperTest_fallbackToMerchantIbanAndHolder(){
        Merchant merchant = MerchantFaker.mockInstance(1);
        merchant.setOperativeEmail("operative.email@test.com");
        merchant.setIban("IT00M1234567890123456789012");
        merchant.setIbanHolder("Merchant Holder");

        Initiative initiative = merchant.getInitiativeList().get(0);
        initiative.setIban("");
        initiative.setIbanHolder("   ");

        MerchantDetailDTO result = mapper.toMerchantDetailDTO(merchant, initiative.getInitiativeId());

        assertAll(() -> {
            assertNotNull(result);
            TestUtils.checkNotNullFields(result);

            assertEquals(merchant.getIban(), result.getIban());
            assertEquals(merchant.getIbanHolder(), result.getIbanHolder());

            assertEquals(initiative.getInitiativeId(), result.getInitiativeId());
        });
    }

    @Test
    void merchantMapperTest_takesIbanFromInitiative(){
        Merchant merchant = MerchantFaker.mockInstance(1);
        merchant.setOperativeEmail("operative.email@test.com");
        merchant.setIban("IBAN_MERCHANT");
        merchant.setIbanHolder("HOLDER_MERCHANT");

        Initiative initiative = merchant.getInitiativeList().get(0);
        initiative.setIban("IBAN_INIZIATIVA");
        initiative.setIbanHolder("HOLDER_INIZIATIVA");

        MerchantDetailDTO result = mapper.toMerchantDetailDTO(merchant, initiative.getInitiativeId());

        assertAll(() -> {
            assertNotNull(result);
            TestUtils.checkNotNullFields(result);
            assertEquals("IBAN_INIZIATIVA", result.getIban());
            assertEquals("HOLDER_INIZIATIVA", result.getIbanHolder());
        });
    }

    @Test
    void merchantMapperTest_fallbackToMerchantWhenInitiativeFieldsAreNull(){
        Merchant merchant = MerchantFaker.mockInstance(1);
        merchant.setOperativeEmail("operative.email@test.com");
        merchant.setIban("IBAN_MERCHANT");
        merchant.setIbanHolder("HOLDER_MERCHANT");

        Initiative initiative = merchant.getInitiativeList().get(0);
        initiative.setIban(null);
        initiative.setIbanHolder(null);

        MerchantDetailDTO result = mapper.toMerchantDetailDTO(merchant, initiative.getInitiativeId());

        assertAll(() -> {
            assertNotNull(result);
            TestUtils.checkNotNullFields(result);
            assertEquals("IBAN_MERCHANT", result.getIban());
            assertEquals("HOLDER_MERCHANT", result.getIbanHolder());
        });
    }

    @Test
    void toMerchantDetailDTOWithoutInitiativeTest() {
        Merchant merchant = MerchantFaker.mockInstance(1);

        MerchantDetailDTO result = mapper.toMerchantDetailDTOWithoutInitiative(merchant);
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(merchant.getVatNumber(), result.getVatNumber()),
                () -> assertEquals(merchant.getIban(), result.getIban())
        );
    }
}
