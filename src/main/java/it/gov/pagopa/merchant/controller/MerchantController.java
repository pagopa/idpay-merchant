package it.gov.pagopa.merchant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/idpay/merchant")
public interface MerchantController {

    @GetMapping("/merchantId/{fiscalCode}/{acquirerId}")
    @ResponseStatus(code = HttpStatus.OK)
    String retrieveMerchantId(@PathVariable("fiscalCode") String fiscalCode, @PathVariable("acquirerId") String acquirerId);
}