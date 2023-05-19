package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.service.MerchantDetailService;
import it.gov.pagopa.merchant.service.MerchantListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MerchantControllerImpl implements MerchantController {

  private final MerchantListService merchantListService;
  private final MerchantDetailService merchantDetailService;

  public MerchantControllerImpl(MerchantListService merchantListService, MerchantDetailService merchantDetailService) {
    this.merchantListService = merchantListService;
    this.merchantDetailService = merchantDetailService;
  }

  @Override
  public ResponseEntity<MerchantListDTO> getMerchantList(String initiativeId, String fiscalCode, Pageable pageable) {
    return ResponseEntity.ok(merchantListService.getMerchantList(initiativeId, fiscalCode, pageable));
  }

  @Override
  public ResponseEntity<MerchantDetailDTO> getMerchantDetail(String initiativeId, String merchantId) {

    return ResponseEntity.ok(merchantDetailService.getMerchantDetail(initiativeId, merchantId));
  }
}
