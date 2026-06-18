package it.gov.pagopa.merchant.connector.pdnd.connector;

import static it.gov.pagopa.merchant.connector.pdnd.constant.PdndConst.PDND_VISURA_TOKEN_CACHE;

import it.gov.pagopa.merchant.connector.pdnd.rest.PDNDRestClient;
import it.gov.pagopa.merchant.connector.pdnd.dto.PDNDTokenForm;
import it.gov.pagopa.merchant.connector.pdnd.dto.ClientCredentialsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PDNDVisuraConnectorImpl {
    private final PDNDRestClient pdndRestClient;

    public PDNDVisuraConnectorImpl(PDNDRestClient pdndRestClient) {
        this.pdndRestClient = pdndRestClient;
    }

    @Cacheable(value = PDND_VISURA_TOKEN_CACHE, key = "#clientId", cacheManager = PDND_VISURA_TOKEN_CACHE)
    public ClientCredentialsResponse createToken(String clientAssertion, String clientAssertionType, String grantType, String clientId) {
        log.debug(" clientAssertionType = {}, grantType = {}, clientId = {}", clientAssertionType, grantType, clientId);
        PDNDTokenForm form = PDNDTokenForm.builder()
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