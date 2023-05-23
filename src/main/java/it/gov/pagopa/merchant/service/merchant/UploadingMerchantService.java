package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.dto.StorageEventDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadingMerchantService {

    MerchantUpdateDTO uploadMerchantFile(
            MultipartFile file,
            String organizationId,
            String initiativeId);

    void ingestionMerchantFile(StorageEventDTO storageEventDto);

}
