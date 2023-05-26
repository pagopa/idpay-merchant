package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.dto.InitiativeDTO;
import java.time.LocalDateTime;

public class InitiativeDTOFaker {

    public static InitiativeDTO mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static InitiativeDTO.InitiativeDTOBuilder mockInstanceBuilder(Integer bias) {
        return InitiativeDTO.builder()
                .initiativeId("INITIATIVEID%d".formatted(bias))
                .initiativeName("INITIATIVENAME%d".formatted(bias))
                .merchantStatus("MERCHANTSTATUS%d".formatted(bias))
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .enabled(true);
    }
}
