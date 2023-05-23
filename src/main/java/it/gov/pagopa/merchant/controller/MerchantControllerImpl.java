package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class MerchantControllerImpl implements MerchantController {

  private final MerchantService merchantService;

  public MerchantControllerImpl(MerchantService merchantService) {
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

  @Override
  public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
    log.info("[GET_MERCHANT_INITIATIVE_LIST] Merchant {} requested to retrieve his initiative list", merchantId);
    return merchantService.getMerchantInitiativeList(merchantId);
  }
}
