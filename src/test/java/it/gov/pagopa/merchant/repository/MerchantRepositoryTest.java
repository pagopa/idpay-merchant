package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.BaseIntegrationTest;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.merchant.test.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MerchantRepositoryTest extends BaseIntegrationTest {

    @Autowired
    protected MerchantRepository merchantRepository;

    private final String MERCHANT_ID = "MERCHANT_ID_TEST";

    @AfterEach
    void clearTestData(){
        merchantRepository.deleteById(MERCHANT_ID);
    }
    @Test
    void findByAcquirerIdAndFiscalCode() {
        Merchant merchant = MerchantFaker.mockInstance(1);
        merchant.setMerchantId(MERCHANT_ID);

        merchantRepository.save(merchant);

        Merchant merchantRetrieved = merchantRepository.findByAcquirerIdAndFiscalCode(merchant.getAcquirerId(), merchant.getFiscalCode()).orElse(null);

        Assertions.assertAll(() -> {
            Assertions.assertNotNull(merchantRetrieved);
            Assertions.assertEquals(merchant.getMerchantId(), merchantRetrieved.getMerchantId());
            TestUtils.checkNotNullFields(merchantRetrieved,
                    "fiscalCode",
                    "acquirerId",
                    "businessName",
                    "legalOfficeAddress",
                    "legalOfficeMunicipality",
                    "legalOfficeProvince",
                    "legalOfficeZipCode",
                    "certifiedEmail",
                    "vatNumber",
                    "iban",
                    "initiativeList");
        });
    }

    @Test
    void findByAcquirerIdAndFiscalCode_NotFound() {
        
        Merchant merchantNotRetrieved = merchantRepository.findByAcquirerIdAndFiscalCode("DUMMYACQUIRERID", "DUMMYFISCALCODE").orElse(null);

        Assertions.assertNull(merchantNotRetrieved);
    }
}