package it.gov.pagopa.merchant.connector.pdnd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import it.gov.pagopa.merchant.connector.pdnd.config.PDNDInfoCamereRestClientConfig;
import it.gov.pagopa.merchant.connector.pdnd.config.PDNDVisuraInfoCamereRestClientConfig;
import it.gov.pagopa.merchant.connector.pdnd.dto.ClientCredentialsResponse;
import it.gov.pagopa.merchant.connector.pdnd.dto.PDNDImpresa;
import it.gov.pagopa.merchant.connector.pdnd.exception.ResourceNotFoundException;
import it.gov.pagopa.merchant.connector.pdnd.extra.rest.PDNDInfoCamereRestClient;
import it.gov.pagopa.merchant.connector.pdnd.rest.PDNDVisuraInfoCamereRawRestClient;
import it.gov.pagopa.merchant.connector.pdnd.utils.DataEncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

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


    private static final String BEARER = "Bearer ";

    public PDNDCacheableService(TokenProviderVisura tokenProviderVisura,
                                PDNDVisuraInfoCamereRawRestClient pdndVisuraInfoCamereRawRestClient,
                                PDNDVisuraInfoCamereRestClientConfig pdndVisuraInfoCamereRestClientConfig
                                /*
                                PDNDInfoCamereRestClient pdndInfoCamereRestClient,
                                TokenProvider tokenProviderPDND,
                                PDNDInfoCamereRestClientConfig pdndInfoCamereRestClientConfig
                                */
    ) {
        this.tokenProviderVisura = tokenProviderVisura;
        this.pdndVisuraInfoCamereRawRestClient = pdndVisuraInfoCamereRawRestClient;
        this.pdndVisuraInfoCamereRestClientConfig = pdndVisuraInfoCamereRestClientConfig;
        /*
        this.pdndInfoCamereRestClient = pdndInfoCamereRestClient;
        this.tokenProviderPDND = tokenProviderPDND;
        this.pdndInfoCamereRestClientConfig = pdndInfoCamereRestClientConfig;
        */
    }

    @Cacheable(cacheNames = "pdndInfocamere", cacheManager = "redisCacheManager", key = "'retrieveInstitutionDetail:' + #encryptedTaxCode")
    public String getEncryptedDocument(String encryptedTaxCode) {
        log.info("getEncryptedDocument for {} START", encryptedTaxCode);
        ClientCredentialsResponse tokenResponse = tokenProviderVisura.getTokenPdnd(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue());
        String bearer = BEARER + tokenResponse.getAccessToken();
        var taxCode = DataEncryptionUtils.decrypt(encryptedTaxCode);
        try {
            byte[] document = pdndVisuraInfoCamereRawRestClient.getRawInstitutionDetail(taxCode, bearer);
            log.info("getEncryptedDocument for {} END", encryptedTaxCode);
            return DataEncryptionUtils.encrypt(new String(document, StandardCharsets.UTF_8));
        } catch (FeignException e) {
            if (e instanceof FeignException.BadRequest) {
                throw new ResourceNotFoundException("No institution found for taxCode: " + taxCode);
            }
            log.error("FeignException occurred while retrieving institution detail", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected exception occurred while retrieving institution detail", e);
            throw new IllegalArgumentException("Unexpected error while retrieving institution detail", e);
        }
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
