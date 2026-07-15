package it.gov.pagopa.merchant.controller.pdnd;

import it.gov.pagopa.merchant.connector.pdnd.PdndVisuraConnectorImpl;
import it.gov.pagopa.merchant.connector.pdnd.rest.PdndRestClient;
import it.gov.pagopa.merchant.dto.pdnd.ClientCredentialsResponse;
import it.gov.pagopa.merchant.dto.pdnd.PdndTokenForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdndVisuraConnectorImplTest {

    @Mock
    private PdndRestClient pdndRestClient;

    private PdndVisuraConnectorImpl pdndVisuraConnector;

    @BeforeEach
    void setUp() {
        pdndVisuraConnector = new PdndVisuraConnectorImpl(pdndRestClient);
    }

    @Test
    void createToken_success() {
        String clientAssertion = "assertion";
        String clientAssertionType = "type";
        String grantType = "grant";
        String clientId = "client123";

        ClientCredentialsResponse mockResponse = new ClientCredentialsResponse();
        when(pdndRestClient.createToken(any(PdndTokenForm.class))).thenReturn(mockResponse);

        ClientCredentialsResponse response = pdndVisuraConnector.createToken(clientAssertion, clientAssertionType, grantType, clientId);

        assertNotNull(response);
        verify(pdndRestClient).createToken(any(PdndTokenForm.class));
    }
}