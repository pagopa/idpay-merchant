package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MerchantFaker {
    private MerchantFaker() {
    }

    public static Merchant mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static Merchant.MerchantBuilder mockInstanceBuilder(Integer bias) {
        Initiative initiative = Initiative.builder()
                .initiativeId("INITIATIVEID%d".formatted(bias))
                .initiativeName("INITIATIVE_NAME")
                .organizationId("ORGANIZATION_ID_%d".formatted(bias))
                .enabled(true)
                .merchantStatus("STATUS")
                .creationDate(LocalDateTime.of(2023,5,22,10, 0))
                .updateDate(LocalDateTime.of(2023,5,22,10, 0)).build();
        ArrayList<Initiative> initiatives = new ArrayList<>();
        initiatives.add(initiative);
        return Merchant.builder()
                .merchantId("MERCHANT_ID_%d".formatted(bias))
                .acquirerId("ACQUIRER_ID_%d".formatted(bias))
                .initiativeList(initiatives)
                .businessName("BUSINESS_NAME")
                .legalOfficeAddress("ADDRESS")
                .legalOfficeMunicipality("MUNICIPALITY")
                .legalOfficeProvince("PROVINCE")
                .legalOfficeZipCode("ZIP_CODE")
                .certifiedEmail("MAIL")
                .fiscalCode("FISCAL_CODE")
                .vatNumber("VAT_NUMBER_%d".formatted(bias))
                .iban("IT00TEST")
                .enabled(true);
    }
}
