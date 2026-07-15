package it.gov.pagopa.merchant.service.pdnd;

import feign.FeignException;
import feign.Request;
import it.gov.pagopa.merchant.configuration.pdnd.PdndVisuraInfoCamereRestClientConfig;
import it.gov.pagopa.merchant.connector.file_storage.MerchantBlobClientImpl;
import it.gov.pagopa.merchant.connector.pdnd.rest.PdndVisuraInfoCamereRawRestClient;
import it.gov.pagopa.merchant.dto.pdnd.ClientCredentialsResponse;
import it.gov.pagopa.merchant.dto.pdnd.PdndSecretValue;
import it.gov.pagopa.merchant.exception.custom.ResourceNotFoundException;
import it.gov.pagopa.merchant.service.pdnd.token.TokenProviderVisura;
import it.gov.pagopa.merchant.utils.DataEncryptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdndCacheableServiceTest {

    @Mock
    private TokenProviderVisura tokenProviderVisura;

    @Mock
    private PdndVisuraInfoCamereRawRestClient pdndVisuraInfoCamereRawRestClient;

    @Mock
    private PdndVisuraInfoCamereRestClientConfig pdndVisuraInfoCamereRestClientConfig;

    @Mock
    private MerchantBlobClientImpl azureBlobClient;

    @Mock
    private PdndSecretValue pdndSecretValue;

    private PdndCacheableService service;

    @BeforeEach
    void setUp() {
        service = new PdndCacheableService(
                tokenProviderVisura,
                pdndVisuraInfoCamereRawRestClient,
                pdndVisuraInfoCamereRestClientConfig,
                azureBlobClient
        );
    }

    @Test
    void getAtecoCodes_shouldReturnAtecoCodes() {

        ClientCredentialsResponse tokenResponse = new ClientCredentialsResponse();
        tokenResponse.setAccessToken("accessToken");

        when(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue())
                .thenReturn(pdndSecretValue);

        when(tokenProviderVisura.getTokenPdnd(pdndSecretValue))
                .thenReturn(tokenResponse);

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <VisuraImpresa>
                    <info-attivita>
                        <classificazioni-ateco>
                            <classificazione-ateco
                                c-attivita="47.11.10"
                                attivita="Commercio al dettaglio"
                                c-importanza="1"/>
                            <classificazione-ateco
                                c-attivita="56.10.11"
                                attivita="Ristorazione"
                                c-importanza="2"/>
                        </classificazioni-ateco>
                    </info-attivita>
                </VisuraImpresa>
                """;

        try (MockedStatic<DataEncryptionUtils> mockedStatic =
                     mockStatic(DataEncryptionUtils.class)) {

            mockedStatic.when(() ->
                            DataEncryptionUtils.decrypt("encryptedFiscalCode"))
                    .thenReturn("12345678901");

            mockedStatic.when(() ->
                            DataEncryptionUtils.encrypt(anyString()))
                    .thenReturn("encryptedXml");

            when(pdndVisuraInfoCamereRawRestClient.getRawInstitutionDetail(
                    ("12345678901"),
                    ("Bearer accessToken")))
                    .thenReturn(xml.getBytes(StandardCharsets.UTF_8));

            List<String> result =
                    service.getAtecoCodes("encryptedFiscalCode");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("47.11.10", result.get(0));
            assertEquals("56.10.11", result.get(1));

            verify(azureBlobClient)
                    .upload(any(), anyString(), eq("application/xml"));
        }
    }

    @Test
    void getAtecoCodes_noAtecoCodes_shouldReturnEmptyList() {

        ClientCredentialsResponse tokenResponse = new ClientCredentialsResponse();
        tokenResponse.setAccessToken("accessToken");

        when(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue())
                .thenReturn(pdndSecretValue);

        when(tokenProviderVisura.getTokenPdnd(pdndSecretValue))
                .thenReturn(tokenResponse);

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <VisuraImpresa>
                    <info-attivita/>
                </VisuraImpresa>
                """;

        try (MockedStatic<DataEncryptionUtils> mockedStatic =
                     mockStatic(DataEncryptionUtils.class)) {

            mockedStatic.when(() ->
                            DataEncryptionUtils.decrypt("encryptedFiscalCode"))
                    .thenReturn("12345678901");

            mockedStatic.when(() ->
                            DataEncryptionUtils.encrypt(anyString()))
                    .thenReturn("encryptedXml");

            when(pdndVisuraInfoCamereRawRestClient.getRawInstitutionDetail(
                    anyString(),
                    anyString()))
                    .thenReturn(xml.getBytes(StandardCharsets.UTF_8));

            List<String> result =
                    service.getAtecoCodes("encryptedFiscalCode");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getAtecoCodes_badRequestShouldThrowResourceNotFoundException() {

        ClientCredentialsResponse tokenResponse = new ClientCredentialsResponse();
        tokenResponse.setAccessToken("accessToken");

        when(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue())
                .thenReturn(pdndSecretValue);

        when(tokenProviderVisura.getTokenPdnd(pdndSecretValue))
                .thenReturn(tokenResponse);

        try (MockedStatic<DataEncryptionUtils> mockedStatic =
                     mockStatic(DataEncryptionUtils.class)) {

            mockedStatic.when(() ->
                            DataEncryptionUtils.decrypt(anyString()))
                    .thenReturn("12345678901");

            Request request = Request.create(
                    Request.HttpMethod.GET,
                    "/test",
                    Map.of(),
                    null,
                    StandardCharsets.UTF_8,
                    null
            );

            FeignException.BadRequest exception =
                    new FeignException.BadRequest(
                            "Bad Request",
                            request,
                            null,
                            Map.of()
                    );

            when(pdndVisuraInfoCamereRawRestClient.getRawInstitutionDetail(
                    anyString(),
                    anyString()))
                    .thenThrow(exception);

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.getAtecoCodes("encryptedFiscalCode")
            );
        }
    }

    @Test
    void getAtecoCodes_internalServerErrorShouldRethrowFeignException() {

        ClientCredentialsResponse tokenResponse = new ClientCredentialsResponse();
        tokenResponse.setAccessToken("accessToken");

        when(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue())
                .thenReturn(pdndSecretValue);

        when(tokenProviderVisura.getTokenPdnd(pdndSecretValue))
                .thenReturn(tokenResponse);

        try (MockedStatic<DataEncryptionUtils> mockedStatic =
                     mockStatic(DataEncryptionUtils.class)) {

            mockedStatic.when(() ->
                            DataEncryptionUtils.decrypt(anyString()))
                    .thenReturn("12345678901");

            Request request = Request.create(
                    Request.HttpMethod.GET,
                    "/test",
                    Map.of(),
                    null,
                    StandardCharsets.UTF_8,
                    null
            );

            FeignException.InternalServerError exception =
                    new FeignException.InternalServerError(
                            "Internal Server Error",
                            request,
                            null,
                            Map.of()
                    );

            when(pdndVisuraInfoCamereRawRestClient.getRawInstitutionDetail(
                    anyString(),
                    anyString()))
                    .thenThrow(exception);

            assertThrows(
                    FeignException.class,
                    () -> service.getAtecoCodes("encryptedTaxCode")
            );
        }
    }

    @Test
    void getAtecoCodes_invalidXmlShouldThrowIllegalArgumentException() {

        ClientCredentialsResponse tokenResponse = new ClientCredentialsResponse();
        tokenResponse.setAccessToken("accessToken");

        when(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue())
                .thenReturn(pdndSecretValue);

        when(tokenProviderVisura.getTokenPdnd(pdndSecretValue))
                .thenReturn(tokenResponse);

        try (MockedStatic<DataEncryptionUtils> mockedStatic =
                     mockStatic(DataEncryptionUtils.class)) {

            mockedStatic.when(() ->
                            DataEncryptionUtils.decrypt(anyString()))
                    .thenReturn("12345678901");

            mockedStatic.when(() ->
                            DataEncryptionUtils.encrypt(anyString()))
                    .thenReturn("encryptedXml");

            when(pdndVisuraInfoCamereRawRestClient.getRawInstitutionDetail(
                    anyString(),
                    anyString()))
                    .thenReturn("invalid xml".getBytes(StandardCharsets.UTF_8));

            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> service.getAtecoCodes("encryptedTaxCode")
                    );

            assertTrue(
                    exception.getMessage()
                            .contains("Unexpected error while retrieving institution detail")
            );
        }
    }
}