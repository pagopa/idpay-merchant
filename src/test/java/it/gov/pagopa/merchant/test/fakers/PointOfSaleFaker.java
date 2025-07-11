package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.model.PointOfSale;

import java.util.List;

public class PointOfSaleFaker {

    public static PointOfSale mockInstance() {
        return mockInstanceBuilder().build();
    }

    public static PointOfSale.PointOfSaleBuilder mockInstanceBuilder() {
        return PointOfSale.builder()
                .type("FISICO")
                .merchantId("MERCHANT-ID")
                .region("REGION")
                .province("PROVINCE")
                .city("CITY")
                .zipCode("ZIPCODE")
                .address("ADDRESS")
                .streetNumber("STREET-NUMBER")
                .contactEmail("EMAIL")
                .contactName("NAME")
                .contactSurname("SURNAME")
                .channels(List.of());
    }
}
