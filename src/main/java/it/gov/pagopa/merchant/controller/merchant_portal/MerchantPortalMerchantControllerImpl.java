package it.gov.pagopa.merchant.controller.merchant_portal;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Slf4j
@RestController
public class MerchantPortalMerchantControllerImpl implements MerchantPortalMerchantController {

    private final MerchantService merchantService;

    public MerchantPortalMerchantControllerImpl(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Override
    public List<InitiativeDTO> getMerchantInitiativeList(String merchantId) {
        List<InitiativeDTO> response = merchantService.getMerchantInitiativeList(merchantId);

        if (response == null) {
            throw new ClientExceptionWithBody(
                    HttpStatus.NOT_FOUND,
                    MerchantConstants.NOT_FOUND,
                    String.format(MerchantConstants.MERCHANT_BY_MERCHANT_ID_MESSAGE, merchantId));
        }
        return response;
    }
}
