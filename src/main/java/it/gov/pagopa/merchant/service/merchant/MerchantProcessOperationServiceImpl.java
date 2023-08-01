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
import org.bson.BsonValue;
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
        long startTime = System.currentTimeMillis();

        if (MerchantConstants.OPERATION_TYPE_DELETE_INITIATIVE.equals(queueCommandOperationDTO.getOperationType())) {

            UpdateResult updateResult = merchantRepository.findAndRemoveInitiativeOnMerchant(queueCommandOperationDTO.getOperationId());
            BsonValue value = updateResult.getUpsertedId();
            log.info("id={}, modifiedCount={}",updateResult.getUpsertedId(), updateResult.getModifiedCount());
            //List<String> merchantId = deletedMerchant.stream().map(Merchant::getMerchantId).toList();
            if (value != null) {
                String merchant = value.asDocument().toJson();
                log.info("[DELETE_MERCHANT] Deleted {} merchant on initiative {}", merchant,
                        queueCommandOperationDTO.getOperationId());
            }
            List<MerchantFile> deletedMerchantFile = merchantFileRepository.deleteByInitiativeId(queueCommandOperationDTO.getOperationId());

            log.info("[DELETE_MERCHANT_FILE] Deleted {} merchant file/s on initiative {}", deletedMerchantFile.size(),
                    queueCommandOperationDTO.getOperationId());

            //merchantId.forEach(mId -> auditUtilities.logDeleteMerchant(mId, queueCommandOperationDTO.getOperationId()));
            deletedMerchantFile.forEach(merchantFile -> auditUtilities.logDeleteMerchantFile(queueCommandOperationDTO.getOperationId()));
        }
        Utilities.performanceLog(startTime, "DELETE_MERCHANT_AND_MERCHANT_FILE");
    }
}
