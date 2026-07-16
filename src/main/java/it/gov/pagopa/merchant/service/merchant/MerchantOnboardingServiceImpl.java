package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.connector.pdnd.PdndInfoCamereConnectorImpl;
import it.gov.pagopa.merchant.dto.OnboardingResponse;
import it.gov.pagopa.merchant.dto.initiative.InitiativeDTO;
import it.gov.pagopa.merchant.exception.custom.InitiativeInvocationException;
import it.gov.pagopa.merchant.exception.custom.MerchantAlreadyOnboardedException;
import it.gov.pagopa.merchant.exception.custom.MerchantNotEligibleException;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantOnboardingServiceImpl implements MerchantOnboardingService {

    private final MerchantRepository merchantRepository;
    private final InitiativeRestConnector initiativeRestConnector;
    private final PdndInfoCamereConnectorImpl pdndConnector;

    @Override
    public OnboardingResponse onboardMerchant(String merchantId, String initiativeId) {
        validateInputs(merchantId, initiativeId);

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        InitiativeDTO initiative;
        try {
            initiative = initiativeRestConnector.getInitiativeDetail(initiativeId);
        } catch (Exception e) {
            log.error("[ONBOARDING] Failed to fetch initiative details for initiative [{}]", initiativeId, e);
            throw new InitiativeInvocationException("Failed to fetch initiative details");
        }

        if (isAlreadyOnboarded(merchant, initiativeId)) {
            throw new MerchantAlreadyOnboardedException(
                    "Merchant with id [%s] is already onboarded on initiative [%s]"
                            .formatted(merchantId, initiativeId));
        }

        if (!isEligible(merchant, initiative)) {
            throw new MerchantNotEligibleException(
                    "Merchant with id [%s] is not eligible for onboarding on initiative [%s]"
                            .formatted(merchantId, initiativeId));
        }

        LocalDateTime onboardingDate = LocalDateTime.now();

        if (merchant.getInitiativeList() == null || merchant.getInitiativeList().isEmpty()) {
            merchant.setInitiativeList(new ArrayList<>());
        } else if (!(merchant.getInitiativeList() instanceof ArrayList)) {
            merchant.setInitiativeList(new ArrayList<>(merchant.getInitiativeList()));
        }
        merchant.getInitiativeList().add(createMerchantInitiative(initiative));
        saveMerchant(merchant);

        log.info("[ONBOARDING] Merchant [{}] successfully onboarded on initiative [{}]",
                merchantId, initiativeId);

        return OnboardingResponse.builder()
                .initiativeId(initiative.getInitiativeId())
                .status("ONBOARDING_OK")
                .onboardingDate(onboardingDate)
                .build();
    }

    private void validateInputs(String merchantId, String initiativeId) {
        if (merchantId == null || merchantId.isBlank() || initiativeId == null || initiativeId.isBlank()) {
            throw new IllegalArgumentException("Merchant ID and Initiative ID must not be null or empty");
        }
    }

    private boolean isAlreadyOnboarded(Merchant merchant, String initiativeId) {
        return merchant.getInitiativeList().stream()
                .anyMatch(i -> i.getInitiativeId().equals(initiativeId));
    }

    private void saveMerchant(Merchant merchant) {
        merchantRepository.save(merchant);
        log.debug("[ONBOARDING] Merchant [{}] saved successfully", merchant.getMerchantId());
    }

    private boolean isEligible(Merchant merchant, InitiativeDTO initiative) {

        List<String> newAtecoCodes = pdndConnector.retrieveAtecoCodes(merchant.getFiscalCode(), merchant.getAtecoCodes());

        Set<String> currentAteco = merchant.getAtecoCodes() == null
                ? Collections.emptySet()
                : new HashSet<>(merchant.getAtecoCodes());

        if (!new HashSet<>(newAtecoCodes).equals(currentAteco)) {
            merchant.setAtecoCodes(newAtecoCodes);
            merchant.setUpdateDate(LocalDateTime.now());
            merchantRepository.save(merchant);
        }
        Set<String> initiativeAtecoCodes = new HashSet<>(initiative.getAtecoCodes());

        return newAtecoCodes.stream()
                .anyMatch(initiativeAtecoCodes::contains);
    }

    private Initiative createMerchantInitiative(InitiativeDTO dto) {
        return Initiative.builder()
                .initiativeId(dto.getInitiativeId())
                .initiativeName(dto.getInitiativeName())
                .organizationId(dto.getOrganizationId())
                .organizationName(dto.getOrganizationName())
                .serviceId(dto.getAdditionalInfo().getServiceId())
                .startDate(dto.getGeneral().getStartDate())
                .endDate(dto.getGeneral().getEndDate())
                .status(dto.getStatus())
                .merchantStatus("UPLOADED")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .enabled(true)
                .build();
    }
}