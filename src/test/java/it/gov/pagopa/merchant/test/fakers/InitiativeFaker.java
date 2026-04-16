package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.model.Initiative;

import java.time.Instant;

public class InitiativeFaker {

    public static Initiative mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static Initiative.InitiativeBuilder mockInstanceBuilder(Integer bias) {
        return Initiative.builder()
                .initiativeId("INITIATIVEID%d".formatted(bias))
                .initiativeName("INITIATIVENAME%d".formatted(bias))
                .organizationId("ORGANIZATION%d".formatted(bias))
                .organizationName("ORGANIZATIONNAME%d".formatted(bias))
                .status("PUBLISHED")
                .startDate(Instant.now())
                .endDate(Instant.now())
                .serviceId("SERVICEID%d")
                .merchantStatus("MERCHANTSTATUS%d".formatted(bias))
                .creationDate(Instant.now())
                .updateDate(Instant.now())
                .enabled(true);
    }
}
