package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.connector.pdnd.PdndInfoCamereConnectorImpl;
import it.gov.pagopa.merchant.dto.OnboardingResponse;
import it.gov.pagopa.merchant.dto.initiative.AdditionalInfoDTO;
import it.gov.pagopa.merchant.dto.initiative.GeneralInfoDTO;
import it.gov.pagopa.merchant.dto.initiative.InitiativeDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantAlreadyOnboardedException;
import it.gov.pagopa.merchant.exception.custom.MerchantNotEligibleException;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantOnboardingServiceImplTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private InitiativeRestConnector initiativeRestConnector;

    @Mock
    private PdndInfoCamereConnectorImpl pdndConnector;

    private MerchantOnboardingServiceImpl merchantOnboardingService;

    @BeforeEach
    void setUp() {
        merchantOnboardingService = new MerchantOnboardingServiceImpl(
                merchantRepository,
                initiativeRestConnector,
                pdndConnector);
    }

    @Test
    void onboardMerchant_success() {
        String merchantId = "merchant123";
        String initiativeId = "initiative123";

        Merchant merchant = new Merchant();
        merchant.setMerchantId(merchantId);
        merchant.setFiscalCode("123");
        merchant.setInitiativeList(List.of());

        InitiativeDTO initiativeDTO = new InitiativeDTO();
        initiativeDTO.setInitiativeId(initiativeId);
        initiativeDTO.setInitiativeName("name");
        initiativeDTO.setOrganizationId("organizationId");
        initiativeDTO.setOrganizationName("organizationName");
        initiativeDTO.setAdditionalInfo(AdditionalInfoDTO.builder().serviceId("serviceId").build());
        initiativeDTO.setGeneral(GeneralInfoDTO.builder().endDate(LocalDate.now()).startDate(LocalDate.now()).build());
        initiativeDTO.setStatus("Status");
        initiativeDTO.setAtecoCodes(List.of("1234"));

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(initiativeRestConnector.getInitiativeDetail(initiativeId)).thenReturn(initiativeDTO);
        when(pdndConnector.retrieveAtecoCodes(anyString(), any())).thenReturn(List.of("1234"));

        OnboardingResponse response = merchantOnboardingService.onboardMerchant(merchantId, initiativeId);

        assertNotNull(response);
        assertEquals(initiativeId, response.getInitiativeId());
        assertEquals("ONBOARDING_OK", response.getStatus());
        verify(merchantRepository, times(2)).save(merchant);
    }

    @Test
    void onboardMerchant_merchantNotFound() {
        String merchantId = "merchant123";
        String initiativeId = "initiative123";

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        assertThrows(MerchantNotFoundException.class,
                () -> merchantOnboardingService.onboardMerchant(merchantId, initiativeId));
    }

    @Test
    void onboardMerchant_alreadyOnboarded() {
        String merchantId = "merchant123";
        String initiativeId = "initiative123";

        Merchant merchant = new Merchant();
        merchant.setMerchantId(merchantId);
        merchant.setInitiativeList(List.of(Initiative.builder().initiativeId("initiative123").build()));

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));

        assertThrows(MerchantAlreadyOnboardedException.class,
                () -> merchantOnboardingService.onboardMerchant(merchantId, initiativeId));
    }

    @Test
    void onboardMerchant_notEligible() {
        String merchantId = "merchant123";
        String initiativeId = "initiative123";

        Merchant merchant = new Merchant();
        merchant.setMerchantId(merchantId);
        merchant.setFiscalCode("123");
        merchant.setInitiativeList(List.of());

        InitiativeDTO initiativeDTO = new InitiativeDTO();
        initiativeDTO.setInitiativeId(initiativeId);
        initiativeDTO.setAtecoCodes(List.of("5678"));

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(initiativeRestConnector.getInitiativeDetail(initiativeId)).thenReturn(initiativeDTO);
        when(pdndConnector.retrieveAtecoCodes(anyString(), any())).thenReturn(List.of("1234"));

        assertThrows(MerchantNotEligibleException.class,
                () -> merchantOnboardingService.onboardMerchant(merchantId, initiativeId));
    }
}