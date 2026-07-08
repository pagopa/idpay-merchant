package it.gov.pagopa.merchant.connector.initiative;

import it.gov.pagopa.merchant.dto.pdnd.PageResponse;
import it.gov.pagopa.merchant.dto.InitiativeSearchRequest;
import it.gov.pagopa.merchant.dto.initiative.InitiativeDTO;
import it.gov.pagopa.merchant.dto.initiative.InitiativeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
@FeignClient(
        name = "${rest-client.initiative.serviceCode}",
        url = "${rest-client.initiative.baseUrl}")
public interface InitiativeRestClient {

  @GetMapping(
          value = "/idpay/initiative/{initiativeId}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  InitiativeDTO getInitiativeDetail(
          @PathVariable("initiativeId") String initiativeId);

  @PostMapping("/initiatives/search")
  ResponseEntity<PageResponse<InitiativeResponse>> searchInitiatives(
          @RequestBody InitiativeSearchRequest request,
          @SpringQueryMap Pageable pageable);


}