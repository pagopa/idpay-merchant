package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MerchantService {
    MerchantUpdateDTO uploadMerchantFile(MultipartFile file, String organizationId, String initiativeId, String organizationUserId);
    MerchantDetailDTO getMerchantDetail(String organizationId, String initiativeId, String merchantId);
    MerchantDetailDTO getMerchantDetail(String merchantId, String initiativeId);
    MerchantListDTO getMerchantList(String organizationId, String initiativeId, String fiscalCode, Pageable pageable);
    String retrieveMerchantId(String acquirerId, String fiscalCode);
    List<InitiativeDTO> getMerchantInitiativeList(String merchantId);
}
