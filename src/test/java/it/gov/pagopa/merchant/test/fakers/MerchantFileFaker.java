package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.model.MerchantFile;

import java.time.LocalDateTime;

public class MerchantFileFaker {
    private MerchantFileFaker() {
    }

    public static MerchantFile mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static MerchantFile.MerchantFileBuilder mockInstanceBuilder(Integer bias) {
        return MerchantFile.builder()
                .fileName("test.csv")
                .initiativeId("INITIATIVE_ID_%d".formatted(bias))
                .entityId("ORGANIZATION_ID_%d".formatted(bias))
                .organizationUserId("ORGANIZATION _USER_ID")
                .status("STATUS")
                .uploadDate(LocalDateTime.of(2023,5,22,10, 0))
                .enabled(true);

    }
}