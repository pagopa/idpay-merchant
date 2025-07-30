package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;

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
                .website("https://localhost.it")
                .contactEmail("EMAIL@email.it")
                .contactName("NAME")
                .contactSurname("SURNAME")
                .website("https://localhost:8080")
                .channelEmail("EMAIL@email.it")
                .channelGeolink("https://localhost:8080")
                .channelWebsite("https://localhost:8080")
                .channelPhone("3333333333");
    }
}
