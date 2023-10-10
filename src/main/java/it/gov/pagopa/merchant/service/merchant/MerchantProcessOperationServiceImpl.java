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
            List<Merchant> merchantList;
            int count = 0;

            do {
                merchantList = merchantRepository.findByInitiativeIdPageable(initiativeId, pageSize);

                for (Merchant merchant : merchantList) {
                    merchantRepository.findAndRemoveInitiativeOnMerchant(initiativeId, merchant.getMerchantId());
                }

                if(merchantList.size() == (pageSize)){
                    count++;

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("An error has occurred while waiting {}", e.getMessage());
                    }
                }

            } while (merchantList.size() == (pageSize));

            log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant", initiativeId);
            Long modifiedCount =((long) count * pageSize + merchantList.size());
            auditUtilities.logDeleteMerchant(modifiedCount, initiativeId);

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

            deletedOperation.forEach(merchantFile -> auditUtilities.logDeleteMerchantFile(initiativeId));

            Utilities.performanceLog(startTime, "DELETE_INITIATIVE");

        }
    }
}
