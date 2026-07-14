package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.OnboardingResponse;

public interface MerchantOnboardingService {

    OnboardingResponse onboardMerchant(String merchantId, String initiativeId);
}
