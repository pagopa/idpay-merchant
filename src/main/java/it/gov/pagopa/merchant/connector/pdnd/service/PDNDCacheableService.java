package it.gov.pagopa.merchant.connector.pdnd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import feign.FeignException;
import it.gov.pagopa.merchant.connector.file_storage.MerchantBlobClientImpl;
import it.gov.pagopa.merchant.connector.pdnd.config.PDNDVisuraInfoCamereRestClientConfig;
import it.gov.pagopa.merchant.connector.pdnd.dto.*;
import it.gov.pagopa.merchant.connector.pdnd.exception.ResourceNotFoundException;
import it.gov.pagopa.merchant.connector.pdnd.rest.PDNDVisuraInfoCamereRawRestClient;
import it.gov.pagopa.merchant.connector.pdnd.utils.DataEncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PDNDCacheableService {

   /*
    private final TokenProvider tokenProviderPDND;
    private final PDNDInfoCamereRestClientConfig pdndInfoCamereRestClientConfig;
    private final PDNDInfoCamereRestClient pdndInfoCamereRestClient;
    */
    private final TokenProviderVisura tokenProviderVisura;
    private final PDNDVisuraInfoCamereRawRestClient pdndVisuraInfoCamereRawRestClient;
    private final PDNDVisuraInfoCamereRestClientConfig pdndVisuraInfoCamereRestClientConfig;
    private final MerchantBlobClientImpl azureBlobClient;
    private static final String BEARER = "Bearer ";

    public PDNDCacheableService(TokenProviderVisura tokenProviderVisura,
                                PDNDVisuraInfoCamereRawRestClient pdndVisuraInfoCamereRawRestClient,
                                PDNDVisuraInfoCamereRestClientConfig pdndVisuraInfoCamereRestClientConfig,
                                /*
                                PDNDInfoCamereRestClient pdndInfoCamereRestClient,
                                TokenProvider tokenProviderPDND,
                                PDNDInfoCamereRestClientConfig pdndInfoCamereRestClientConfig
                                */
                                MerchantBlobClientImpl azureBlobClient) {
        this.tokenProviderVisura = tokenProviderVisura;
        this.pdndVisuraInfoCamereRawRestClient = pdndVisuraInfoCamereRawRestClient;
        this.pdndVisuraInfoCamereRestClientConfig = pdndVisuraInfoCamereRestClientConfig;
        /*
        this.pdndInfoCamereRestClient = pdndInfoCamereRestClient;
        this.tokenProviderPDND = tokenProviderPDND;
        this.pdndInfoCamereRestClientConfig = pdndInfoCamereRestClientConfig;
        */
        this.azureBlobClient = azureBlobClient;
    }

    @Cacheable(cacheNames = "pdndAtecoCodes", cacheManager = "redisCacheManager",
            key = "'atecoCodes:' + #encryptedTaxCode")
    public List<String> getAtecoCodes(String encryptedTaxCode) {
        log.info("getAtecoCodes for {} START", encryptedTaxCode);
        ClientCredentialsResponse tokenResponse = tokenProviderVisura.getTokenPdnd(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue());
        String bearer = BEARER + tokenResponse.getAccessToken();
        var taxCode = DataEncryptionUtils.decrypt(encryptedTaxCode);
        try {
            byte[] document = pdndVisuraInfoCamereRawRestClient.getRawInstitutionDetail(taxCode, bearer);
            String decDocument = new String(document, StandardCharsets.UTF_8);
            saveVisuraToStorage(decDocument, taxCode);
            PDNDVisuraImpresa parsed = xmlToVisuraImpresa(document);

            log.info("getAtecoCodes for {} END", encryptedTaxCode);
            return extractAtecoCodes(parsed);
        } catch (FeignException e) {
            if (e instanceof FeignException.BadRequest) {
                throw new ResourceNotFoundException("No institution found for taxCode: " + encryptedTaxCode);
            }
            log.error("FeignException occurred while retrieving institution detail", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception occurred while retrieving institution detail", e);
            throw new IllegalArgumentException("Unexpected error while retrieving institution detail", e);
        }
    }

    private void saveVisuraToStorage(String decDocument, String taxCode) {
        try (InputStream is = new ByteArrayInputStream(decDocument.getBytes(StandardCharsets.UTF_8))) {
            azureBlobClient.upload(is, "visura_" + taxCode + "_" + LocalDateTime.now() + ".xml", "application/xml");
        } catch (IOException e) {
            log.error("Unable to save visura to storage for taxCode {}", taxCode, e);
        }
    }

    private PDNDVisuraImpresa xmlToVisuraImpresa(byte[] xmlBytes) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.findAndRegisterModules();
        return xmlMapper.readValue(xmlBytes, PDNDVisuraImpresa.class);
    }

    private List<String> extractAtecoCodes(PDNDVisuraImpresa parsed) {
        return Optional.ofNullable(parsed.getInfoAttivita())
                .map(InfoAttivita::getClassificazioniAteco)
                .map(ClassificazioniAteco::getClassificazioniAteco)
                .orElse(Collections.emptyList())
                .stream()
                .map(ClassificazioneAteco::getCodiceAttivita)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /*
    @Cacheable(cacheNames = "pdndInfocamere", cacheManager = "redisCacheManager", key = "'retrieveInstitutionPdndByTaxCode:' + #encryptedTaxCode")
    public String getEncryptedPDNDImpresa(String encryptedTaxCode) {
        log.info("getEncryptedPDNDImpresa for {} START", encryptedTaxCode);
        String taxCode = DataEncryptionUtils.decrypt(encryptedTaxCode);

        ClientCredentialsResponse tokenResponse = tokenProviderPDND.getTokenPdnd(pdndInfoCamereRestClientConfig.getPdndSecretValue());
        String bearer = BEARER + tokenResponse.getAccessToken();

        try {
            List<PDNDImpresa> imprese = pdndInfoCamereRestClient.retrieveInstitutionPdndByTaxCode(taxCode, bearer);
            if (Objects.isNull(imprese) || imprese.isEmpty()) {
                throw new ResourceNotFoundException("No institution found for taxCode: " + taxCode);
            }
            int lastUpdatedIndex = imprese.size() - 1;
            log.info("InfoCamere returned {} records for taxCode {}, selecting last updated index {}", imprese.size(), taxCode, lastUpdatedIndex);
            PDNDImpresa impresa = imprese.get(lastUpdatedIndex);
            return DataEncryptionUtils.encrypt(new ObjectMapper().writeValueAsString(impresa));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception occurred while retrieving institution", e);
            throw new IllegalArgumentException("Unexpected error while retrieving institution", e);
        }
    }
    */
}
