package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MerchantControllerImpl implements MerchantController {

  private final MerchantService merchantService;

  public MerchantControllerImpl(MerchantService merchantService){
    this.merchantService = merchantService;
  }

  @Override
  public ResponseEntity<MerchantListDTO> getMerchantList(String initiativeId, String fiscalCode, Pageable pageable) {
    return ResponseEntity.ok(merchantService.getMerchantList(initiativeId, fiscalCode, pageable));
  }

  @Override
  public ResponseEntity<MerchantDetailDTO> getMerchantDetail(String initiativeId, String merchantId) {
    return ResponseEntity.ok(merchantService.getMerchantDetail(initiativeId, merchantId));
  }
}
