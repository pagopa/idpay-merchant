package it.gov.pagopa.merchant.controller.acquirer;

import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Slf4j
@RestController

public class AcquirerControllerImpl implements AcquirerController {

    private final MerchantService merchantService;

    public AcquirerControllerImpl(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Override
    public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
        List<InitiativeDTO> response = merchantService.getMerchantInitiativeList(merchantId);

        if (response == null) {
            throw new MerchantNotFoundException(
                    ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE);
        }
        return response;
    }

    @Override
    public ResponseEntity<MerchantUpdateDTO> uploadMerchantFile(MultipartFile file,
                                                                String acquirerId,
                                                                String initiativeId) {
        return ResponseEntity.ok(merchantService.uploadMerchantFile(file, acquirerId, initiativeId,null, acquirerId));
    }
}
