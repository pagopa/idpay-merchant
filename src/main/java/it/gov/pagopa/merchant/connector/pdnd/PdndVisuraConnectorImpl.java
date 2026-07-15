package it.gov.pagopa.merchant.connector.pdnd;

import static it.gov.pagopa.merchant.constants.PdndConst.PDND_VISURA_TOKEN_CACHE;
import static it.gov.pagopa.merchant.constants.PdndConst.REDIS_CACHE_MANAGER;

import it.gov.pagopa.merchant.connector.pdnd.rest.PdndRestClient;
import it.gov.pagopa.merchant.dto.pdnd.PdndTokenForm;
import it.gov.pagopa.merchant.dto.pdnd.ClientCredentialsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PdndVisuraConnectorImpl {
    private final PdndRestClient pdndRestClient;

    public PdndVisuraConnectorImpl(PdndRestClient pdndRestClient) {
        this.pdndRestClient = pdndRestClient;
    }

    @Cacheable(value = PDND_VISURA_TOKEN_CACHE, key = "#clientId", cacheManager = REDIS_CACHE_MANAGER)
    public ClientCredentialsResponse createToken(String clientAssertion, String clientAssertionType, String grantType, String clientId) {
        log.debug(" clientAssertionType = {}, grantType = {}, clientId = {}", clientAssertionType, grantType, clientId);
        PdndTokenForm form = PdndTokenForm.builder()
                .clientAssertion(clientAssertion)
                .clientAssertionType(clientAssertionType)
                .grantType(grantType)
                .clientId(clientId)
                .build();
        ClientCredentialsResponse result = pdndRestClient.createToken(form);
        log.debug("PdndClientCredentialsResponse result = {}", result);
        return result;
    }

}