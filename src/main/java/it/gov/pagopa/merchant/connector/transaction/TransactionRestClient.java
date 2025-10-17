package it.gov.pagopa.merchant.connector.transaction;

import it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(
    name = "transactions",
    url = "${rest-client.transactions.baseUrl}")
public interface TransactionRestClient {

  @GetMapping(value = "/idpay/merchant/portal/initiatives/{initiativeId}/transactions/processed",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  MerchantTransactionsListDTO getPointOfSaleTransactions(
      @RequestHeader("x-merchant-id") String merchantId,
      @PathVariable String initiativeId,
      @RequestParam(required = false) String fiscalCode,
      @RequestParam(required = false) String status,
      Pageable pageable
  );
}
