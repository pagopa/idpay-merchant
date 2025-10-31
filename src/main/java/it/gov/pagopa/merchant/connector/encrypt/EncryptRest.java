package it.gov.pagopa.merchant.connector.encrypt;


import it.gov.pagopa.merchant.dto.CFDTO;
import it.gov.pagopa.merchant.dto.EncryptedCfDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "${rest-client.encryptpdv.cf}", url = "${rest-client.encryptpdv.baseUrl}")
public interface EncryptRest {

  @PutMapping(value = "/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  EncryptedCfDTO upsertToken(@RequestBody CFDTO cfdto, @RequestHeader("x-api-key") String apikey);
}
