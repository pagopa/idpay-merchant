package it.gov.pagopa.merchant.connector.transaction;

import it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO;
import it.gov.pagopa.merchant.dto.transaction.RewardTransaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

@FeignClient(
    name = "transactions",
    url = "${rest-client.transactions.baseUrl}")
public interface TransactionRestClient {

  @GetMapping(value = "/idpay/merchant/portal/initiatives/{initiativeId}/transactions/processed",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  MerchantTransactionsListDTO getMerchantTransactions(
      @RequestHeader("x-merchant-id") String merchantId,
      @PathVariable String initiativeId,
      @RequestParam(required = false) String fiscalCode,
      @RequestParam(required = false) String status,
      Pageable pageable
  );

  @GetMapping(value = "/idpay/transactions",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  RewardTransaction findAll(
          @RequestParam(value = "idTrxIssuer", required = false) String idTrxIssuer,
          @RequestParam(value = "userId", required = false) String userId,
          @RequestParam(value = "trxDateStart", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime trxDateStart,
          @RequestParam(value = "trxDateEnd", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime trxDateEnd,
          @RequestParam(value = "amountCents", required = false) Long amountCents,
          @PageableDefault(size = 2000) Pageable pageable
  );
}
