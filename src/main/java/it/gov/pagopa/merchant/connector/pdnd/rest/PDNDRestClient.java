package it.gov.pagopa.merchant.connector.pdnd.rest;


import it.gov.pagopa.merchant.connector.pdnd.dto.ClientCredentialsResponse;
import it.gov.pagopa.merchant.connector.pdnd.dto.PDNDTokenForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "${rest-client.pdnd.serviceCode}", url = "${rest-client.pdnd.base-url}")
public interface PDNDRestClient {
    @PostMapping(value = "/token.oauth2", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ClientCredentialsResponse createToken(PDNDTokenForm form);
}