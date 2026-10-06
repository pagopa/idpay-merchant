package it.gov.pagopa.merchant.connector.pdnd.rest;


import it.gov.pagopa.merchant.dto.pdnd.ClientCredentialsResponse;
import it.gov.pagopa.merchant.dto.pdnd.PdndTokenForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "${rest-client.pdnd.serviceCode}", url = "${rest-client.pdnd.base-url}")
public interface PdndRestClient {
    @PostMapping(value = "/token.oauth2", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ClientCredentialsResponse createToken(PdndTokenForm form);
}