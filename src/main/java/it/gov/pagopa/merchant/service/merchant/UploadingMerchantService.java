package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import org.springframework.messaging.Message;
import org.springframework.web.multipart.MultipartFile;

public interface UploadingMerchantService {

    MerchantUpdateDTO uploadMerchantFile(
            MultipartFile file,
            String organizationId,
            String initiativeId,
            String organizationUserId,
            String acquirerId);

    void execute(Message<String> message);

}
