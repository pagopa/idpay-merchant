package it.gov.pagopa.merchant.controller.merchant_portal;

import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
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
        return merchantService.getMerchantInitiativeList(merchantId);
    }

    @Override
    public MerchantDetailDTO getMerchantDetail(String merchantId, String initiativeId) {
        log.info("[GET_MERCHANT_DETAIL] Get merchant with id {} for initiative {}", merchantId, initiativeId);
        MerchantDetailDTO merchantDetail = merchantService.getMerchantDetail(merchantId, initiativeId);
        if(merchantDetail == null){
            throw new MerchantNotFoundException(
                    ExceptionCode.MERCHANT_NOT_ONBOARDED,
                    String.format(ExceptionMessage.INITIATIVE_AND_MERCHANT_NOT_FOUND, initiativeId)
            );
        }
        return merchantDetail;
    }
}
