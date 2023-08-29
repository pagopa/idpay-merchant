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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MerchantProcessOperationServiceImpl implements MerchantProcessOperationService{

    private final MerchantRepository merchantRepository;
    private final MerchantFileRepository merchantFileRepository;
    private final AuditUtilities auditUtilities;

    public MerchantProcessOperationServiceImpl(MerchantFileRepository merchantFileRepository, MerchantRepository merchantRepository, AuditUtilities auditUtilities) {
        this.merchantRepository = merchantRepository;
        this.merchantFileRepository = merchantFileRepository;
        this.auditUtilities = auditUtilities;
    }

    @Override
    public void processOperation(QueueCommandOperationDTO queueCommandOperationDTO) {

        if (MerchantConstants.OPERATION_TYPE_DELETE_INITIATIVE.equals(queueCommandOperationDTO.getOperationType())) {
            long startTime = System.currentTimeMillis();

            UpdateResult updateResult = merchantRepository.findAndRemoveInitiativeOnMerchant(queueCommandOperationDTO.getEntityId());

            log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant", queueCommandOperationDTO.getEntityId());
            List<MerchantFile> deletedMerchantFile = merchantFileRepository.deleteByInitiativeId(queueCommandOperationDTO.getEntityId());

            log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: merchant_file",
                    queueCommandOperationDTO.getEntityId());

            auditUtilities.logDeleteMerchant(updateResult.getModifiedCount(), queueCommandOperationDTO.getEntityId());
            deletedMerchantFile.forEach(merchantFile -> auditUtilities.logDeleteMerchantFile(queueCommandOperationDTO.getEntityId()));
            Utilities.performanceLog(startTime, "DELETE_INITIATIVE");
        }
    }
}
