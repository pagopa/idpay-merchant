package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.dto.InitiativeDTO;
import java.time.Instant;

public class InitiativeDTOFaker {

    public static InitiativeDTO mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static InitiativeDTO.InitiativeDTOBuilder mockInstanceBuilder(Integer bias) {
        return InitiativeDTO.builder()
                .initiativeId("INITIATIVEID%d".formatted(bias))
                .initiativeName("INITIATIVENAME%d".formatted(bias))
                .merchantStatus("MERCHANTSTATUS%d".formatted(bias))
                .creationDate(Instant.now())
                .updateDate(Instant.now())
                .enabled(true);
    }
}
