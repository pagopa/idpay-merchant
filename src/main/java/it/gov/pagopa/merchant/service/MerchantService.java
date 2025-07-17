package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MerchantService {
    MerchantUpdateDTO uploadMerchantFile(MultipartFile file, String organizationId, String initiativeId, String organizationUserId, String acquirerId);
    MerchantDetailDTO getMerchantDetail(String organizationId, String initiativeId, String merchantId);
    MerchantDetailDTO getMerchantDetail(String merchantId, String initiativeId);
    MerchantListDTO getMerchantList(String organizationId, String initiativeId, String fiscalCode, Pageable pageable);
    MerchantDetailDTO updateIban(String merchantId, String initiativeId, MerchantIbanPatchDTO merchantIbanPatchDTO);
    String retrieveMerchantId(String acquirerId, String fiscalCode);
    List<InitiativeDTO> getMerchantInitiativeList(String merchantId);

    void processOperation(QueueCommandOperationDTO queueCommandOperationDTO);
    void updatingInitiative(QueueInitiativeDTO queueInitiativeDTO);

}
