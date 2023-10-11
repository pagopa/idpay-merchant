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

    @Override
    public void processOperation(QueueCommandOperationDTO queueCommandOperationDTO) {

        if (MerchantConstants.OPERATION_TYPE_DELETE_INITIATIVE.equals(queueCommandOperationDTO.getOperationType())) {
            long startTime = System.currentTimeMillis();

            String initiativeId = queueCommandOperationDTO.getEntityId();

            deleteMerchant(initiativeId);

            deleteMerchantFile(initiativeId);

            Utilities.performanceLog(startTime, "DELETE_INITIATIVE");

        }
    }

    @SuppressWarnings("BusyWait")
    private void deleteMerchantFile(String initiativeId) {
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


        deletedOperation.forEach(merchantFile -> auditUtilities.logDeleteMerchantFile(initiativeId));
    }

    @SuppressWarnings("BusyWait")
    private void deleteMerchant(String initiativeId) {
        List<Merchant> merchantList;

        do {
            merchantList = merchantRepository.findByInitiativeIdPageable(initiativeId, pageSize);

            for (Merchant merchant : merchantList) {
                merchantRepository.findAndRemoveInitiativeOnMerchant(initiativeId, merchant.getMerchantId());
                auditUtilities.logDeleteMerchant(merchant.getMerchantId(), initiativeId);
            }

            log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant", initiativeId);

            if(merchantList.size() == (pageSize)){

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("An error has occurred while waiting {}", e.getMessage());
                }
            }

        } while (merchantList.size() == (pageSize));
    }
}
