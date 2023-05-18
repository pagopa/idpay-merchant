package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.model.Initiative;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RequestMapping("/idpay/merchant")
public interface MerchantController {

    @GetMapping("/initiatives")
    @ResponseStatus(code = HttpStatus.OK)
    List<Initiative> getMerchantInitiativeList(
            @RequestParam String merchantId,
            @RequestParam(required = false) Boolean enabled);
}