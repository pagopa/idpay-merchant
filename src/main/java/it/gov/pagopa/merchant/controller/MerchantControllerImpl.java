package it.gov.pagopa.merchant.controller;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.utils.AuditUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class MerchantControllerImpl implements MerchantController {

  private final MerchantService merchantService;
  private final MerchantRepository merchantRepository;
  private final MerchantFileRepository merchantFileRepository;
  private final AuditUtilities auditUtilities;
  @Value("${app.delete.paginationSize}")
  private int pageSize;
  @Value("${app.delete.delayTime}")
  private long delay;

  public MerchantControllerImpl(MerchantService merchantService, MerchantRepository merchantRepository, MerchantFileRepository merchantFileRepository, AuditUtilities auditUtilities) {
    this.merchantService = merchantService;
    this.merchantRepository = merchantRepository;
    this.merchantFileRepository = merchantFileRepository;
    this.auditUtilities = auditUtilities;
  }

  @Override
  public ResponseEntity<MerchantUpdateDTO> uploadMerchantFile(MultipartFile file,
                                                              String organizationId,
                                                              String initiativeId,
                                                              String organizationUserId) {
    return ResponseEntity.ok(merchantService.uploadMerchantFile(file, organizationId, initiativeId, organizationUserId, "PAGOPA"));
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
  public void processOperation(String initiativeId) {
      UpdateResult updateResult;
      /*do {
        updateResult = merchantRepository.findAndRemoveInitiativeOnMerchant(initiativeId,
                pageSize);

        try {
          Thread.sleep(delay);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.error("An error has occurred while waiting {}", e.getMessage());
        }

      } while (updateResult.getModifiedCount() == pageSize);

      log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant", initiativeId);

       */
    List<Merchant> merchant = merchantRepository.findByInitiativeIdWithBatch(initiativeId, pageSize);

    for (Merchant merchant1 : merchant) {
      merchantRepository.findAndRemoveInitiativeOnMerchantTest(initiativeId, merchant1.getMerchantId());
      try {
        Thread.sleep(delay/pageSize);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("An error has occurred while waiting {}", e.getMessage());
      }
    }

    log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant", initiativeId);

      List<MerchantFile> deletedOperation = new ArrayList<>();
      List<MerchantFile> fetchedMerchantsFile;

      do {
        fetchedMerchantsFile = merchantFileRepository.deletePaged(initiativeId,
                pageSize);

        deletedOperation.addAll(fetchedMerchantsFile);

        try {
          Thread.sleep(delay);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.error("An error has occurred while waiting {}", e.getMessage());
        }

      } while (fetchedMerchantsFile.size() == (pageSize));

      log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant_file",
              initiativeId);

      //auditUtilities.logDeleteMerchant(updateResult.getModifiedCount(), initiativeId);

      deletedOperation.forEach(merchantFile -> auditUtilities.logDeleteMerchantFile(initiativeId));

  }
}
