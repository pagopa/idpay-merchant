package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.model.Merchant;

import java.util.List;

public class MerchantFaker {

    public static Merchant mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static Merchant.MerchantBuilder mockInstanceBuilder(Integer bias) {
        return Merchant.builder()
                .merchantId("MERCHANTID%d".formatted(bias))
                .acquirerId("ACQUIRERID%d".formatted(bias))
                .businessName("BUSINESSNAME%d".formatted(bias))
                .merchantName("MERCHANTNAME%d".formatted(bias))
                .legalOfficeAddress("LEGALOFFICEADDREDD%d".formatted(bias))
                .legalOfficeMunicipality("LEGALOFFICEMUNICIPALITY%d".formatted(bias))
                .legalOfficeProvince("LEGALOFFICEPROVINCE%d".formatted(bias))
                .legalOfficeZipCode("LEGALOFFICEZIPCODE%d".formatted(bias))
                .certifiedEmail("CERTIFIEDEMAIL%d".formatted(bias))
                .fiscalCode("FISCALCODE%d".formatted(bias))
                .vatNumber("VATNUMBER%d".formatted(bias))
                .iban("IBAN%d".formatted(bias))
                .initiativeList(List.of())
                .enabled(true);
    }
}
