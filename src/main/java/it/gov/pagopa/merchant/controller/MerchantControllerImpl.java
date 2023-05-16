package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MerchantControllerImpl implements MerchantController {

  private final MerchantService merchantService;

  public MerchantControllerImpl(MerchantService merchantService) {
    this.merchantService = merchantService;
  }
}