package it.gov.pagopa.merchant.connector;

import com.github.tomakehurst.wiremock.WireMockServer;
import it.gov.pagopa.merchant.configuration.RestConnectorConfig;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnectorImpl;
import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(
        classes = {
                InitiativeRestConnectorImpl.class,
                RestConnectorConfig.class,
                FeignAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
                "spring.application.name=idpay-initiative-integration-rest",
                "rest-client.initiative.baseUrl=http://localhost:8089"
        })
class InitiativeRestClientTest {

    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String INITIATIVE_ID_NOT_FOUND = "INITIATIVE_ID_NOT_FOUND";

    private static WireMockServer wireMockServer;

    @Autowired
    private InitiativeRestConnector restConnector;


    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setupStubs() {

        // risposta OK
        stubFor(get(urlEqualTo("/idpay/initiative/" + INITIATIVE_ID + "/beneficiary/view"))
                .willReturn(okJson("""
                        {
                          "initiativeId": "INITIATIVE_ID"
                        }
                        """)));

        // risposta 404
        stubFor(get(urlEqualTo("/idpay/initiative/" + INITIATIVE_ID_NOT_FOUND + "/beneficiary/view"))
                .willReturn(aResponse()
                        .withStatus(404)));
    }

    @Test
    void getInitiativeBeneficiaryView() {
        InitiativeBeneficiaryViewDTO actual =
                restConnector.getInitiativeBeneficiaryView(INITIATIVE_ID);

        assertNotNull(actual);
        assertEquals(INITIATIVE_ID, actual.getInitiativeId());
    }

    @Test
    void getInitiativeNotFound() {
        InitiativeBeneficiaryViewDTO actual =
                restConnector.getInitiativeBeneficiaryView(INITIATIVE_ID_NOT_FOUND);

        assertNull(actual);
    }

}
