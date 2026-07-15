package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.dto.initiative.AdditionalInfoDTO;
import it.gov.pagopa.merchant.dto.initiative.GeneralInfoDTO;
import it.gov.pagopa.merchant.dto.initiative.InitiativeDTO;

import java.time.LocalDate;

public class InitiativeBeneficiaryViewDTOFaker {
    private InitiativeBeneficiaryViewDTOFaker() {
    }

    public static InitiativeDTO mockInstance(Integer bias) {
        return mockInstanceBuilder(bias).build();
    }

    public static InitiativeDTO.InitiativeDTOBuilder mockInstanceBuilder(Integer bias) {
        return InitiativeDTO.builder()
                .initiativeId("INITIATIVEID%d".formatted(bias))
                .initiativeName("INITIATIVENAME")
                .organizationId("ORGANIZATIONID%d".formatted(bias))
                .organizationName("ORGANIZATIONNAME")
                .additionalInfo(AdditionalInfoDTO.builder().serviceId("SERVICEID%d".formatted(bias)).build())
                .general(GeneralInfoDTO.builder().startDate(LocalDate.now()).endDate(LocalDate.now()).build())
                .status("PUBLISHED")
                ;


    }
}