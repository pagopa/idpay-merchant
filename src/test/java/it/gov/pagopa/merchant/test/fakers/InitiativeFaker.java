package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.model.Initiative;

import java.time.LocalDateTime;

public class InitiativeFaker {

    public static Initiative mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static Initiative.InitiativeBuilder mockInstanceBuilder(Integer bias) {
        return Initiative.builder()
                .initiativeId("INITIATIVEID%d".formatted(bias))
                .initiativeName("INITIATIVENAME%d".formatted(bias))
                .merchantStatus("MERCHANTSTATUS%d".formatted(bias))
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .enabled(true);
    }
}
