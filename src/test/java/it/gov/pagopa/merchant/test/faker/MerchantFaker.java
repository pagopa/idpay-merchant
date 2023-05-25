package it.gov.pagopa.merchant.test.faker;

import it.gov.pagopa.merchant.model.Merchant;

public class MerchantFaker {
    public static Merchant mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static Merchant.MerchantBuilder mockInstanceBuilder(Integer bias) {
        return Merchant.builder()
                .merchantId("MERCHANTID%d".formatted(bias))
                .acquirerId("ACQUIRERID%d".formatted(bias))
                .businessName("BUSINESSNAME%d".formatted(bias))
                .legalOfficeAddress("LEGALOFFICEADDRESS%d".formatted(bias))
                .legalOfficeMunicipality("LEGALOFFICEMUNICIPALITY%d".formatted(bias))
                .legalOfficeProvince("LEGALOFFICEPROVICE%d".formatted(bias))
                .legalOfficeZipCode("LEGALOFFICEZIPCODE%d".formatted(bias))
                .certifiedEmail("CERTIFIEDEMAIL%d".formatted(bias))
                .fiscalCode("FISCALCODE%d".formatted(bias))
                .vatNumber("VATNUMBER%d".formatted(bias))
                .iban("IBAN%d".formatted(bias))
                .enabled(true);
    }
}
