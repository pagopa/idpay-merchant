package it.gov.pagopa.merchant.connector;

import it.gov.pagopa.merchant.configuration.RestConnectorConfig;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnectorImpl;
import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(
        classes = {
                InitiativeRestConnectorImpl.class,
                RestConnectorConfig.class,
                FeignAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class
        })
@AutoConfigureWireMock(stubs = "classpath:/mappings", port = 0)
@TestPropertySource(
        locations = "classpath:application.yml",
        properties = {
                "spring.application.name=idpay-initiative-integration-rest",
                "rest-client.initiative.baseUrl=http://localhost:${wiremock.server.port}",
        })
class InitiativeRestClientTest {

    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String INITIATIVE_ID_NOT_FOUND = "INITIATIVE_ID_NOT_FOUND";

    @Autowired
    private InitiativeRestConnector restConnector;

    @Test
    void getInitiativeBeneficiaryView() {
        InitiativeBeneficiaryViewDTO actual = restConnector.getInitiativeBeneficiaryView(INITIATIVE_ID);
        assertEquals(INITIATIVE_ID, actual.getInitiativeId());
    }

    @Test
    void getInitiativeNotFound() {
        InitiativeBeneficiaryViewDTO actual = restConnector.getInitiativeBeneficiaryView(INITIATIVE_ID_NOT_FOUND);
        assertNull(actual);
    }

}
