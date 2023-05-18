package it.gov.pagopa.merchant.service;


import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@WebMvcTest(value = MerchantService.class)
class MerchantServiceTest {

    public static final String MERCHANT_ID = "MERCHANT_ID";
    @MockBean
    private MerchantRepository merchantRepository;

    @Autowired
    private MerchantService merchantService;

    @Test
    void getMerchantInitiativeList_ok() {
        Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
                        .initiativeList(List.of(
                                InitiativeFaker.mockInstance(1),
                                InitiativeFaker.mockInstance(2)))
                        .build();

        when(merchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(merchant));

        List<Initiative> result = merchantService.getMerchantInitiativeList(MERCHANT_ID, null);

        assertEquals(merchant.getInitiativeList(), result);
    }

    @Test
    void getMerchantInitiativeList_enabledFalse_ok() {
        Initiative initiativeNotEnabled = InitiativeFaker.mockInstanceBuilder(1).enabled(false).build();
        Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
                        .initiativeList(List.of(
                                InitiativeFaker.mockInstance(1),
                                initiativeNotEnabled))
                        .build();

        when(merchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(merchant));

        List<Initiative> result = merchantService.getMerchantInitiativeList(MERCHANT_ID, false);

        assertEquals(List.of(initiativeNotEnabled), result);
    }


}
