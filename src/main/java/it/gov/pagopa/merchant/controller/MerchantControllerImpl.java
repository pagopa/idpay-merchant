package it.gov.pagopa.merchant.controller;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.*;
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
    String sanitizedOrganizationId = sanitizeString(organizationId);
    String sanitizedInitiativeId = sanitizeString(initiativeId);

    return ResponseEntity.ok(
            merchantService.uploadMerchantFile(file, sanitizedOrganizationId, sanitizedInitiativeId,
                    organizationUserId, "PAGOPA"));
  }

  @Override
  public ResponseEntity<MerchantListDTO> getMerchantList(String organizationId, String initiativeId,
                                                         String fiscalCode, Pageable pageable) {
    String sanitizedOrganizationId = sanitizeString(organizationId);
    String sanitizedInitiativeId = sanitizeString(initiativeId);
    String sanitizedFiscalCode = fiscalCode != null ? sanitizeString(fiscalCode) : null;

    return ResponseEntity.ok(
            merchantService.getMerchantList(sanitizedOrganizationId, sanitizedInitiativeId,
                    sanitizedFiscalCode, pageable));
  }

  @Override
  public ResponseEntity<MerchantDetailDTO> getMerchantDetail(String organizationId,
                                                             String initiativeId, String merchantId) {
    String sanitizedOrganizationId = sanitizeString(organizationId);
    String sanitizedInitiativeId = sanitizeString(initiativeId);
    String sanitizedMerchantId = sanitizeString(merchantId);

    return ResponseEntity.ok(
            merchantService.getMerchantDetail(sanitizedOrganizationId, sanitizedInitiativeId,
                    sanitizedMerchantId));
  }

  @Override
  public ResponseEntity<MerchantDetailDTO> updateIban(String merchantId, String organizationId,
                                                      String initiativeId, MerchantIbanPatchDTO merchantIbanPatchDTO) {
    String sanitizedMerchantId = sanitizeString(merchantId);
    String sanitizedOrganizationId = sanitizeString(organizationId);
    String sanitizedInitiativeId = sanitizeString(initiativeId);

    log.info("[UPDATE_IBAN] Request to update iban for merchant {} on initiative {}",
            sanitizedMerchantId, sanitizedInitiativeId);
    MerchantDetailDTO merchantDetailDTO = merchantService.updateIban(sanitizedMerchantId,
            sanitizedOrganizationId, sanitizedInitiativeId, merchantIbanPatchDTO);
    return ResponseEntity.ok(merchantDetailDTO);
  }

  public String retrieveMerchantId(String acquirerId, String fiscalCode) {
    String sanitizedAcquirerId = sanitizeString(acquirerId);
    String sanitizedFiscalCode = sanitizeString(fiscalCode);

    log.info("[GET_MERCHANT_ID] The Merchant with {}, {} requested to retrieve merchantId",
            sanitizedAcquirerId, sanitizedFiscalCode);
    String merchantId = merchantService.retrieveMerchantId(sanitizedAcquirerId,
            sanitizedFiscalCode);
    if (merchantId == null) {
      throw new MerchantNotFoundException(ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE);
    }
    return merchantId;
  }

  @Override
  public String createMerchant(
          String acquirerId,
          String businessName,
          String fiscalCode) {
    MerchantDetailDTO detailDTO = MerchantDetailDTO.builder()
            .businessName(businessName)
            .fiscalCode(fiscalCode)
            .build();

    String merchantId = merchantService.createMerchantIfNotExists(detailDTO, acquirerId);

    MerchantDetailDTO responseDTO = new MerchantDetailDTO();
    responseDTO.setBusinessName(merchantId);

    return merchantId; //TODO resituire il corretto merchantId creato con la funzione Utilities.toUUID([FISCAL_CODE].concat("_").concat([ACQUIRER_ID]))
    }
}
