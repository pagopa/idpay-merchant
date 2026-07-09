package it.gov.pagopa.merchant.service.pdnd.token;

import it.gov.pagopa.merchant.connector.pdnd.PdndVisuraConnectorImpl;
import it.gov.pagopa.merchant.dto.pdnd.ClientCredentialsResponse;
import it.gov.pagopa.merchant.dto.pdnd.JwtConfig;
import it.gov.pagopa.merchant.dto.pdnd.PdndSecretValue;
import it.gov.pagopa.merchant.service.pdnd.assertion.AssertionGeneratorVisura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenProviderVisuraTest {

    @Mock
    private AssertionGeneratorVisura assertionGeneratorVisura;

    @Mock
    private PdndVisuraConnectorImpl pdndVisuraClient;

    private TokenProviderVisura tokenProviderVisura;

    @BeforeEach
    void setUp() {
        tokenProviderVisura = new TokenProviderVisura(
                assertionGeneratorVisura,
                pdndVisuraClient,
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

        when(assertionGeneratorVisura.generateClientAssertion(
                jwtConfig,
                "secretKey"))
                .thenReturn("clientAssertion");

        when(pdndVisuraClient.createToken(
                "clientAssertion",
                "clientAssertionType",
                "grantType",
                "clientId"))
                .thenReturn(expectedResponse);

        ClientCredentialsResponse result =
                tokenProviderVisura.getTokenPdnd(pdndSecretValue);

        assertEquals(expectedResponse, result);

        verify(assertionGeneratorVisura, times(1))
                .generateClientAssertion(jwtConfig, "secretKey");

        verify(pdndVisuraClient, times(1))
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

        when(assertionGeneratorVisura.generateClientAssertion(
                jwtConfig,
                "secretKey"))
                .thenThrow(new RuntimeException("Assertion error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tokenProviderVisura.getTokenPdnd(pdndSecretValue)
        );

        assertEquals("Assertion error", exception.getMessage());

        verify(pdndVisuraClient, never())
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

        when(assertionGeneratorVisura.generateClientAssertion(
                jwtConfig,
                "secretKey"))
                .thenReturn("clientAssertion");

        when(pdndVisuraClient.createToken(
                anyString(),
                anyString(),
                anyString(),
                anyString()))
                .thenThrow(new RuntimeException("PDND error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tokenProviderVisura.getTokenPdnd(pdndSecretValue)
        );

        assertEquals("PDND error", exception.getMessage());

        verify(assertionGeneratorVisura, times(1))
                .generateClientAssertion(jwtConfig, "secretKey");
    }
}