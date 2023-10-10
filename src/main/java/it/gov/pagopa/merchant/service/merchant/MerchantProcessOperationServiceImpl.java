package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.utils.AuditUtilities;
import it.gov.pagopa.merchant.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MerchantProcessOperationServiceImpl implements MerchantProcessOperationService{

    private final MerchantRepository merchantRepository;
    private final MerchantFileRepository merchantFileRepository;
    private final AuditUtilities auditUtilities;


    private final int pageSize;

    private final long delay;

    public MerchantProcessOperationServiceImpl(MerchantFileRepository merchantFileRepository,
                                               MerchantRepository merchantRepository,
                                               AuditUtilities auditUtilities,
                                               @Value("${app.delete.paginationSize}") int pageSize,
                                               @Value("${app.delete.delayTime}") long delay) {
        this.merchantRepository = merchantRepository;
        this.merchantFileRepository = merchantFileRepository;
        this.auditUtilities = auditUtilities;
        this.pageSize = pageSize;
        this.delay = delay;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void processOperation(QueueCommandOperationDTO queueCommandOperationDTO) {

        if (MerchantConstants.OPERATION_TYPE_DELETE_INITIATIVE.equals(queueCommandOperationDTO.getOperationType())) {
            long startTime = System.currentTimeMillis();
            String initiativeId = queueCommandOperationDTO.getEntityId();
            /*UpdateResult updateResult;
            do {
                updateResult = merchantRepository.findAndRemoveInitiativeOnMerchant(queueCommandOperationDTO.getEntityId(),
                        pageSize);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("An error has occurred while waiting {}", e.getMessage());
                }

            } while (updateResult.getModifiedCount() == pageSize);

            List<Merchant> merchant = merchantRepository.findByInitiativeIdWithBatch(queueCommandOperationDTO.getEntityId(), pageSize);

            for (Merchant merchant1 : merchant) {
                merchantRepository.findAndRemoveInitiativeOnMerchantTest(queueCommandOperationDTO.getEntityId(), merchant1.getMerchantId());
                try {
                    Thread.sleep(delay/pageSize);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("An error has occurred while waiting {}", e.getMessage());
                }
            }

            List<MerchantFile> deletedOperation = new ArrayList<>();
            List<MerchantFile> fetchedMerchantsFile;

            do {
                fetchedMerchantsFile = merchantFileRepository.deletePaged(queueCommandOperationDTO.getEntityId(),
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
                    queueCommandOperationDTO.getEntityId());

            //auditUtilities.logDeleteMerchant(updateResult.getModifiedCount(), queueCommandOperationDTO.getEntityId());

            deletedOperation.forEach(merchantFile -> auditUtilities.logDeleteMerchantFile(queueCommandOperationDTO.getEntityId()));

             */

            List<Merchant> merchantList;
            int count = pageSize;

            while (count == pageSize){
                merchantList = merchantRepository.findByInitiativeIdPageable(initiativeId, pageSize);

                for (Merchant merchant : merchantList) {
                    merchantRepository.findAndRemoveInitiativeOnMerchantTest(initiativeId, merchant.getMerchantId());
                }
                count = merchantList.size();

                log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant", initiativeId);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("An error has occurred while waiting {}", e.getMessage());
                }

            }

            /*

            do {
                merchantList = merchantRepository.findByInitiativeIdPageable(initiativeId, pageSize);

                for (Merchant merchant : merchantList) {
                    merchantRepository.findAndRemoveInitiativeOnMerchantTest(initiativeId, merchant.getMerchantId());
                }
                count = merchantList.size();

                log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant", initiativeId);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("An error has occurred while waiting {}", e.getMessage());
                }

            } while (count == (pageSize));

            List<MerchantFile> deletedOperation = new ArrayList<>();
            List<MerchantFile> fetchedMerchantsFile;

            do {
                fetchedMerchantsFile = merchantFileRepository.deletePaged(initiativeId,
                        pageSize);

                deletedOperation.addAll(fetchedMerchantsFile);

                log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant_file",
                        initiativeId);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("An error has occurred while waiting {}", e.getMessage());
                }

            } while (fetchedMerchantsFile.size() == (pageSize));
             */

            List<MerchantFile> deletedOperation = new ArrayList<>();
            List<MerchantFile> fetchedMerchantsFile;
            int countMerchantFile = pageSize;

            while (countMerchantFile == pageSize) {
                fetchedMerchantsFile = merchantFileRepository.deletePaged(initiativeId,
                        pageSize);
                countMerchantFile = fetchedMerchantsFile.size();

                deletedOperation.addAll(fetchedMerchantsFile);

                log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant_file",
                        initiativeId);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("An error has occurred while waiting {}", e.getMessage());
                }
            }

            //auditUtilities.logDeleteMerchant(updateResult.getModifiedCount(), initiativeId);

            deletedOperation.forEach(merchantFile -> auditUtilities.logDeleteMerchantFile(initiativeId));

            Utilities.performanceLog(startTime, "DELETE_INITIATIVE");

        }
    }
}
