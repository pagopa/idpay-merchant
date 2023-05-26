package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    return ResponseEntity.ok(merchantService.uploadMerchantFile(file, organizationId, initiativeId, organizationUserId));
  }
  @Override
  public ResponseEntity<MerchantListDTO> getMerchantList(String organizationId, String initiativeId, String fiscalCode, Pageable pageable) {
    return ResponseEntity.ok(merchantService.getMerchantList(organizationId, initiativeId, fiscalCode, pageable));
  }

  @Override
  public ResponseEntity<MerchantDetailDTO> getMerchantDetail(String organizationId, String initiativeId, String merchantId) {
    return ResponseEntity.ok(merchantService.getMerchantDetail(organizationId, initiativeId, merchantId));
  }

  public String retrieveMerchantId(String acquirerId, String fiscalCode) {
    log.info("[GET_MERCHANT_ID] The Merchant with {}, {} requested to retrieve merchantId", acquirerId , fiscalCode);
    String merchantId = merchantService.retrieveMerchantId(acquirerId, fiscalCode);
    if(merchantId == null){
      throw new ClientExceptionWithBody(
              HttpStatus.NOT_FOUND,
              MerchantConstants.NOT_FOUND,
              String.format(MerchantConstants.MERCHANTID_BY_ACQUIRERID_AND_FISCALCODE_MESSAGE, acquirerId, fiscalCode
      ));
    }
    return merchantId;
  }

  @Override
  public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
    List<InitiativeDTO> response = merchantService.getMerchantInitiativeList(merchantId);

    if (response == null) {
      throw new ClientExceptionWithBody(
              HttpStatus.NOT_FOUND,
              MerchantConstants.NOT_FOUND,
              String.format(MerchantConstants.MERCHANT_BY_MERCHANT_ID_MESSAGE, merchantId));
    }
    return response;
  }
}
