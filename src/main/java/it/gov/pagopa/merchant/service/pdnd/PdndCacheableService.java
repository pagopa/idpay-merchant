package it.gov.pagopa.merchant.service.pdnd;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import feign.FeignException;
import it.gov.pagopa.merchant.configuration.pdnd.PdndVisuraInfoCamereRestClientConfig;
import it.gov.pagopa.merchant.connector.file_storage.MerchantBlobClientImpl;
import it.gov.pagopa.merchant.connector.pdnd.rest.PdndVisuraInfoCamereRawRestClient;
import it.gov.pagopa.merchant.dto.pdnd.*;
import it.gov.pagopa.merchant.exception.custom.ResourceNotFoundException;
import it.gov.pagopa.merchant.service.pdnd.token.TokenProviderVisura;
import it.gov.pagopa.merchant.utils.DataEncryptionUtils;
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

import static it.gov.pagopa.merchant.utils.DataEncryptionUtils.decrypt;
import static it.gov.pagopa.merchant.utils.DataEncryptionUtils.encrypt;

@Service
@Slf4j
public class PdndCacheableService {

    private final TokenProviderVisura tokenProviderVisura;
    private final PdndVisuraInfoCamereRawRestClient pdndVisuraInfoCamereRawRestClient;
    private final PdndVisuraInfoCamereRestClientConfig pdndVisuraInfoCamereRestClientConfig;
    private final MerchantBlobClientImpl azureBlobClient;
    private static final String BEARER = "Bearer ";

    public PdndCacheableService(TokenProviderVisura tokenProviderVisura,
                                PdndVisuraInfoCamereRawRestClient pdndVisuraInfoCamereRawRestClient,
                                PdndVisuraInfoCamereRestClientConfig pdndVisuraInfoCamereRestClientConfig,
                                MerchantBlobClientImpl azureBlobClient) {
        this.tokenProviderVisura = tokenProviderVisura;
        this.pdndVisuraInfoCamereRawRestClient = pdndVisuraInfoCamereRawRestClient;
        this.pdndVisuraInfoCamereRestClientConfig = pdndVisuraInfoCamereRestClientConfig;
        this.azureBlobClient = azureBlobClient;
    }

    @Cacheable(cacheNames = "pdndAtecoCodes", cacheManager = "redisCacheManager",
            key = "'atecoCodes:' + #encryptedFiscalCode")
    public List<String> getAtecoCodes(String encFiscalCode) {
        log.info("getAtecoCodes for {} START", encFiscalCode);
        ClientCredentialsResponse tokenResponse = tokenProviderVisura.getTokenPdnd(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue());
        String bearer = BEARER + tokenResponse.getAccessToken();
        var fiscalCode = decrypt(encFiscalCode);
        try {
            byte[] document = pdndVisuraInfoCamereRawRestClient.getRawInstitutionDetail(fiscalCode, bearer);
            String decDocument = encrypt(new String(document, StandardCharsets.UTF_8));
            saveVisuraToStorage(decDocument, fiscalCode, encFiscalCode);
            PdndVisuraImpresa parsed = xmlToVisuraImpresa(document);

            log.info("getAtecoCodes for {} END", encFiscalCode);
            return extractAtecoCodes(parsed);
        } catch (FeignException e) {
            if (e instanceof FeignException.BadRequest) {
                throw new ResourceNotFoundException("No institution found for fiscalCode: " + encFiscalCode);
            }
            log.error("FeignException occurred while retrieving institution detail", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception occurred while retrieving institution detail", e);
            throw new IllegalArgumentException("Unexpected error while retrieving institution detail", e);
        }
    }

    private void saveVisuraToStorage(String decDocument, String fiscalCode, String encFiscalCode) {
        try (InputStream is = new ByteArrayInputStream(decDocument.getBytes(StandardCharsets.UTF_8))) {
            azureBlobClient.upload(is, "visura_" + fiscalCode + "_" + LocalDateTime.now() + ".xml", "application/xml");
        } catch (IOException e) {
            log.error("Unable to save visura to storage for taxCode {}", encFiscalCode, e);
        }
    }

    private PdndVisuraImpresa xmlToVisuraImpresa(byte[] xmlBytes) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.findAndRegisterModules();
        return xmlMapper.readValue(xmlBytes, PdndVisuraImpresa.class);
    }

    private List<String> extractAtecoCodes(PdndVisuraImpresa parsed) {
        return Optional.ofNullable(parsed.getInfoAttivita())
                .map(InfoAttivita::getClassificazioniAteco)
                .map(ClassificazioniAteco::getClassificazioniAteco)
                .orElse(Collections.emptyList())
                .stream()
                .map(ClassificazioneAteco::getCodiceAttivita)
                .filter(Objects::nonNull)
                .toList();
    }


}
