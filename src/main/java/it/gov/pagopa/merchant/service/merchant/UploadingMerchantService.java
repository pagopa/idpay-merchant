package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.dto.StorageEventDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadingMerchantService {

    MerchantUpdateDTO uploadMerchantFile(
            MultipartFile file,
            String organizationId,
            String initiativeId,
            String organizationUserId,
            String acquirerId);

    void ingestionMerchantFile(List<StorageEventDTO> storageEventDto);

}
