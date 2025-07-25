package it.gov.pagopa.merchant.test.fakers;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.bson.types.ObjectId;

public class PointOfSaleFaker {

    public static PointOfSale mockInstance() {
        return mockInstanceBuilder().build();
    }

    public static PointOfSale.PointOfSaleBuilder mockInstanceBuilder() {
        return PointOfSale.builder()
                .id(new ObjectId())
                .type("PHYSICAL")
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
                .website("https://localhost:8080")
                .channelEmail("EMAIL@email.it")
                .channelGeolink("https://localhost:8080")
                .channelWebsite("https://localhost:8080")
                .channelPhone("3333333333");
    }
}
