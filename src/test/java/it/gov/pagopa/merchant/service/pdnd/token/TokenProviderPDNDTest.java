package it.gov.pagopa.merchant.service.pdnd.token;

import it.gov.pagopa.merchant.connector.pdnd.PdndConnectorImpl;
import it.gov.pagopa.merchant.dto.pdnd.ClientCredentialsResponse;
import it.gov.pagopa.merchant.dto.pdnd.JwtConfig;
import it.gov.pagopa.merchant.dto.pdnd.PdndSecretValue;
import it.gov.pagopa.merchant.service.pdnd.assertion.AssertionGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenProviderPDNDTest {

    @Mock
    private AssertionGenerator assertionGenerator;

    @Mock
    private PdndConnectorImpl pdndClient;

    private TokenProviderPDND tokenProviderPDND;

    @BeforeEach
    void setUp() {
        tokenProviderPDND = new TokenProviderPDND(
                assertionGenerator,
                pdndClient,
                "clientAssertionType",
                "grantType"
        );
    }

    @Test
    void getTokenPdnd_shouldReturnClientCredentialsResponse() {

        JwtConfig jwtConfig = JwtConfig.builder()
                .subject("subject")
                .issuer("issuer")
                .audience("audience")
                .kid("kid")
                .purposeId("purposeId")
                .build();

        PdndSecretValue pdndSecretValue = PdndSecretValue.builder()
                .jwtConfig(jwtConfig)
                .clientId("clientId")
                .secretKey("secretKey")
                .build();

        ClientCredentialsResponse expectedResponse =
                new ClientCredentialsResponse();

        expectedResponse.setAccessToken("accessToken");
        expectedResponse.setTokenType("Bearer");
        expectedResponse.setExpiresIn(3600);

        when(assertionGenerator.generateClientAssertion(
                jwtConfig,
                "secretKey"))
                .thenReturn("clientAssertion");

        when(pdndClient.createToken(
                "clientAssertion",
                "clientAssertionType",
                "grantType",
                "clientId"))
                .thenReturn(expectedResponse);

        ClientCredentialsResponse result =
                tokenProviderPDND.getTokenPdnd(pdndSecretValue);

        assertEquals(expectedResponse, result);

        verify(assertionGenerator, times(1))
                .generateClientAssertion(jwtConfig, "secretKey");

        verify(pdndClient, times(1))
                .createToken(
                        "clientAssertion",
                        "clientAssertionType",
                        "grantType",
                        "clientId"
                );
    }

    @Test
    void getTokenPdnd_shouldPropagateAssertionGeneratorException() {

        JwtConfig jwtConfig = JwtConfig.builder()
                .subject("subject")
                .issuer("issuer")
                .audience("audience")
                .kid("kid")
                .purposeId("purposeId")
                .build();

        PdndSecretValue pdndSecretValue = PdndSecretValue.builder()
                .jwtConfig(jwtConfig)
                .clientId("clientId")
                .secretKey("secretKey")
                .build();

        when(assertionGenerator.generateClientAssertion(
                jwtConfig,
                "secretKey"))
                .thenThrow(new RuntimeException("Assertion error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tokenProviderPDND.getTokenPdnd(pdndSecretValue)
        );

        assertEquals("Assertion error", exception.getMessage());

        verify(pdndClient, never())
                .createToken(any(), any(), any(), any());
    }

    @Test
    void getTokenPdnd_shouldPropagatePdndClientException() {

        JwtConfig jwtConfig = JwtConfig.builder()
                .subject("subject")
                .issuer("issuer")
                .audience("audience")
                .kid("kid")
                .purposeId("purposeId")
                .build();

        PdndSecretValue pdndSecretValue = PdndSecretValue.builder()
                .jwtConfig(jwtConfig)
                .clientId("clientId")
                .secretKey("secretKey")
                .build();

        when(assertionGenerator.generateClientAssertion(
                jwtConfig,
                "secretKey"))
                .thenReturn("clientAssertion");

        when(pdndClient.createToken(
                anyString(),
                anyString(),
                anyString(),
                anyString()))
                .thenThrow(new RuntimeException("PDND error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tokenProviderPDND.getTokenPdnd(pdndSecretValue)
        );

        assertEquals("PDND error", exception.getMessage());

        verify(assertionGenerator, times(1))
                .generateClientAssertion(jwtConfig, "secretKey");
    }
}