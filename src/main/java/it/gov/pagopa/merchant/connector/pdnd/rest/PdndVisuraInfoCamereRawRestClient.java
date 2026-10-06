package it.gov.pagopa.merchant.connector.pdnd.rest;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "${rest-client.pdnd-visura-infocamere-raw.serviceCode}", url = "${rest-client.pdnd-visura-infocamere.base-url}")
public interface PdndVisuraInfoCamereRawRestClient {

    @GetMapping(value = "${rest-client.pdnd-visura-infocamere.getTaxCode.path}")
    byte[] getRawInstitutionDetail(@RequestParam("codiceFiscale") String taxCode,
                                                   @RequestHeader("Authorization") String accessToken);
}
