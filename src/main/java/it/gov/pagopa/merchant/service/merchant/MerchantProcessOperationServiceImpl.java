package it.gov.pagopa.merchant.service.merchant;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
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


    private final String pagination;

    private final String delay;

    public MerchantProcessOperationServiceImpl(MerchantFileRepository merchantFileRepository, MerchantRepository merchantRepository, AuditUtilities auditUtilities, @Value("${app.delete.paginationSize}") String pagination, @Value("${app.delete.delayTime}") String delay) {
        this.merchantRepository = merchantRepository;
        this.merchantFileRepository = merchantFileRepository;
        this.auditUtilities = auditUtilities;
        this.pagination = pagination;
        this.delay = delay;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void processOperation(QueueCommandOperationDTO queueCommandOperationDTO) {

        if (MerchantConstants.OPERATION_TYPE_DELETE_INITIATIVE.equals(queueCommandOperationDTO.getOperationType())) {
            long startTime = System.currentTimeMillis();
            UpdateResult updateResult;
            do {
                updateResult = merchantRepository.findAndRemoveInitiativeOnMerchant(queueCommandOperationDTO.getEntityId(),
                        Integer.parseInt(pagination));

                try {
                    Thread.sleep(Long.parseLong(delay));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("An error has occurred while waiting {}", e.getMessage());
                }

            } while (updateResult.getModifiedCount() == (Integer.parseInt(pagination)));

            log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant", queueCommandOperationDTO.getEntityId());

            List<MerchantFile> deletedOperation = new ArrayList<>();
            List<MerchantFile> fetchedMerchantsFile;

            do {
                fetchedMerchantsFile = merchantFileRepository.deletePaged(queueCommandOperationDTO.getEntityId(),
                        Integer.parseInt(pagination));

                deletedOperation.addAll(fetchedMerchantsFile);

                try {
                    Thread.sleep(Long.parseLong(delay));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("An error has occurred while waiting {}", e.getMessage());
                }

            } while (fetchedMerchantsFile.size() == (Integer.parseInt(pagination)));

            log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant_file",
                    queueCommandOperationDTO.getEntityId());

            auditUtilities.logDeleteMerchant(updateResult.getModifiedCount(), queueCommandOperationDTO.getEntityId());

            deletedOperation.forEach(merchantFile -> auditUtilities.logDeleteMerchantFile(queueCommandOperationDTO.getEntityId()));

            Utilities.performanceLog(startTime, "DELETE_INITIATIVE");
        }
    }
}
