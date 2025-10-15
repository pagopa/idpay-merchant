package it.gov.pagopa.merchant.connector.payment;

import it.gov.pagopa.merchant.connector.payment.dto.PointOfSaleTransactionsListDTO;
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
        value = "/idpay/initiatives/{initiativeId}/point-of-sales/{pointOfSaleId}/transactions",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    PointOfSaleTransactionsListDTO getPointOfSaleTransactions(
        @RequestHeader("x-merchant-id") String merchantId,
        @PathVariable("initiativeId") String initiativeId,
        @PathVariable("pointOfSaleId") String pointOfSaleId,
        @RequestParam(required = false) String fiscalCode,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String productGtin,
        Pageable pageable);
}
