package it.gov.pagopa.merchant.controller.merchant_portal;

import it.gov.pagopa.merchant.connector.pdnd.dto.PageResponse;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.*;
import it.gov.pagopa.merchant.dto.initiative.InitiativeResponse;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.service.ReportedUserService;
import it.gov.pagopa.merchant.service.merchant.MerchantOnboardingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

@Slf4j
@RestController
public class MerchantPortalMerchantControllerImpl implements MerchantPortalMerchantController {

    private final MerchantService merchantService;
    private final ReportedUserService reportedUserService;
    private final MerchantOnboardingService merchantOnboardingService;

    public MerchantPortalMerchantControllerImpl(MerchantService merchantService, ReportedUserService reportedUserService, MerchantOnboardingService merchantOnboardingService) {
        this.merchantService = merchantService;
        this.reportedUserService = reportedUserService;
        this.merchantOnboardingService = merchantOnboardingService;
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

    @Override
    public ResponseEntity<MerchantDetailDTO> patchMerchant(String merchantId,
                                                        String initiativeId, MerchantIbanPatchDTO merchantIbanPatchDTO) {
        String sanitizedMerchantId = sanitizeString(merchantId);
        String sanitizedInitiativeId = sanitizeString(initiativeId);

        log.info("[UPDATE_IBAN] Request to update iban for merchant {} on initiative {}",
                sanitizedMerchantId, sanitizedInitiativeId);
        MerchantDetailDTO merchantDetailDTO = merchantService.patchMerchant(sanitizedMerchantId, sanitizedInitiativeId, merchantIbanPatchDTO);
        return ResponseEntity.ok(merchantDetailDTO);
    }

    @Override
    public ReportedUserCreateResponseDTO createReportedUser(String merchantId, String initiativeId, String userId) {
        return reportedUserService.createReportedUser(userId, merchantId, initiativeId);
    }

    @Override
    public List<ReportedUserDTO> getReportedUser(String merchantId, String initiativeId, String userId
    ) {
        return reportedUserService.searchReportedUser(userId, merchantId, initiativeId);
    }

    @Override
    public ReportedUserCreateResponseDTO deleteReportedUser(String merchantId, String initiativeId, String userId) {

        return reportedUserService.deleteByUserId(userId, merchantId, initiativeId);
    }



    @Override
    public ResponseEntity<PageResponse<InitiativeResponse>> getAvailableInitiatives(
            String merchantId,
            String initiativeName,
            Pageable pageable) {

        Page<InitiativeResponse> page = merchantService.processMerchantInitiatives(merchantId, initiativeName, pageable);

        PageResponse<InitiativeResponse> response = new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }
    @Override
    public ResponseEntity<OnboardingResponse> onboardMerchantInitiative(
            String merchantId,
            String initiativeId) {


        String sanitizedInitiativeId = sanitizeString(initiativeId);
        String sanitizedMerchantId = sanitizeString(merchantId);
        log.info("[ONBOARDING] Onboarding request for merchant [{}] on initiative [{}]",
                sanitizedMerchantId, sanitizedInitiativeId);

        OnboardingResponse response = merchantOnboardingService.onboardMerchant(sanitizedMerchantId, sanitizedInitiativeId);

        return ResponseEntity.ok(response);
    }
}
