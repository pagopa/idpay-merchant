package it.gov.pagopa.merchant.controller;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantIbanPatchDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class MerchantControllerImpl implements MerchantController {

  private final MerchantService merchantService;

  public MerchantControllerImpl(MerchantService merchantService) {
    this.merchantService = merchantService;
  }

  @Override
  public ResponseEntity<MerchantUpdateDTO> uploadMerchantFile(MultipartFile file,
      String organizationId,
      String initiativeId,
      String organizationUserId) {
    return ResponseEntity.ok(
        merchantService.uploadMerchantFile(file, organizationId, initiativeId, organizationUserId,
            "PAGOPA"));
  }

  @Override
  public ResponseEntity<MerchantListDTO> getMerchantList(String organizationId, String initiativeId,
      String fiscalCode, Pageable pageable) {
    return ResponseEntity.ok(
        merchantService.getMerchantList(organizationId, initiativeId, fiscalCode, pageable));
  }

  @Override
  public ResponseEntity<MerchantDetailDTO> getMerchantDetail(String organizationId,
      String initiativeId, String merchantId) {
    return ResponseEntity.ok(
        merchantService.getMerchantDetail(organizationId, initiativeId, merchantId));
  }

  @Override
  public ResponseEntity<MerchantDetailDTO> updateIban(String merchantId, String organizationId,
      String initiativeId, MerchantIbanPatchDTO merchantIbanPatchDTO) {
    log.info("[UPDATE_IBAN] Request to update iban for merchant {} on initiative {}",
        sanitizeString(merchantId), sanitizeString(initiativeId));
    MerchantDetailDTO merchantDetailDTO = merchantService.updateIban(merchantId, organizationId,
        initiativeId,
        merchantIbanPatchDTO);
    return ResponseEntity.ok(merchantDetailDTO);
  }

  public String retrieveMerchantId(String acquirerId, String fiscalCode) {
    log.info("[GET_MERCHANT_ID] The Merchant with {}, {} requested to retrieve merchantId",
        sanitizeString(acquirerId), sanitizeString(fiscalCode));
    String merchantId = merchantService.retrieveMerchantId(acquirerId, fiscalCode);
    if (merchantId == null) {
      throw new MerchantNotFoundException(
          ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE);
    }
    return merchantId;
  }
}
