package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.MerchantInfoDTO;
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
  public ResponseEntity<MerchantListDTO> getMerchantList(String organizationId, String initiativeId, String fiscalCode, Pageable pageable) {
    return ResponseEntity.ok(merchantService.getMerchantList(organizationId, initiativeId, fiscalCode, pageable));
  }

  @Override
  public ResponseEntity<MerchantDetailDTO> getMerchantDetail(String organizationId, String initiativeId, String merchantId) {
    return ResponseEntity.ok(merchantService.getMerchantDetail(organizationId, initiativeId, merchantId));
  }

  public MerchantInfoDTO retrieveMerchantId(String fiscalCode, String acquirerId) {
    log.info("[GET_MERCHANT_ID] The Merchant with {}, {} requested to retrieve merchantId", fiscalCode, acquirerId);
      return merchantService.retrieveMerchantId(fiscalCode, acquirerId);
  }
}
