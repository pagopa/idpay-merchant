package it.gov.pagopa.merchant.connector.payment;

import it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(
    name = "payment",
    url = "${rest-client.payment.baseUrl}")
public interface PaymentRestClient {

    @GetMapping(
        value = "/idpay/merchant/portal/initiatives/{initiativeId}/transactions",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    MerchantTransactionsListDTO getPointOfSaleTransactions(
        @RequestHeader("x-merchant-id") String merchantId,
        @PathVariable("initiativeId") String initiativeId,
        @RequestParam(required = false) String fiscalCode,
        @RequestParam(required = false) String status,
        Pageable pageable);
}
