package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.exception.MerchantException;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    public static final String MERCHANT_ID = "MERCHANT_ID";
    @Mock
    private MerchantRepository merchantRepository;

    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper = new Initiative2InitiativeDTOMapper();

    private MerchantService merchantService;

    @BeforeEach
    void setUp() {
        merchantService = new MerchantServiceImpl(
                merchantRepository,
                initiative2InitiativeDTOMapper);
    }

    @Test
    void getMerchantInitiativeList_ok() {
        Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
                        .initiativeList(List.of(
                                InitiativeFaker.mockInstance(1),
                                InitiativeFaker.mockInstance(2)))
                        .build();

        when(merchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(merchant));

        List<InitiativeDTO> result = merchantService.getMerchantInitiativeList(MERCHANT_ID);

        assertEquals(
                merchant.getInitiativeList().stream()
                        .map(initiative2InitiativeDTOMapper)
                        .toList(),
                result);
    }

    @Test
    void getMerchantInitiativeList_ko() {
        when(merchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());

        try {
            merchantService.getMerchantInitiativeList(MERCHANT_ID);
            fail();
        } catch (MerchantException e) {
            assertEquals(MerchantConstants.Exception.NotFound.CODE, e.getCode());
        }
    }
}
