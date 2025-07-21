package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;

import java.util.List;

public class PointOfSaleDTOFaker {

    public static PointOfSaleDTO mockInstance() {
        return mockInstanceBuilder().build();
    }

    public static PointOfSaleDTO.PointOfSaleDTOBuilder mockInstanceBuilder() {
        return PointOfSaleDTO.builder()
                .type(PointOfSaleTypeEnum.PHYSICAL)
                .franchiseName("FRANCHISE-NAME")
                .region("REGION")
                .province("PROVINCE")
                .city("CITY")
                .zipCode("ZIPCODE")
                .address("ADDRESS")
                .contactEmail("EMAIL")
                .contactName("NAME")
                .contactSurname("SURNAME")
                .channels(List.of());
    }
}
