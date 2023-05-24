package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.merchant.MerchantListService;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {
    @Mock
    private MerchantDetailService merchantDetailServiceMock;
    @Mock
    private MerchantListService merchantListServiceMock;
    @Mock
    private MerchantRepository merchantRepository;

    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper = new Initiative2InitiativeDTOMapper();

    private MerchantServiceImpl merchantService;

    @BeforeEach
    void setUp(){
        merchantService = new MerchantServiceImpl(
                merchantDetailServiceMock,
                merchantListServiceMock,
                merchantRepository,
                initiative2InitiativeDTOMapper);
    }

    @AfterEach
    void verifyNoMoreMockInteractions() {
        Mockito.verifyNoMoreInteractions(
                merchantDetailServiceMock,
                merchantListServiceMock);
    }

    @Test
    void getMerchantDetail(){
        MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
        Mockito.when(merchantService.getMerchantDetail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(dto);

        MerchantDetailDTO result = merchantService.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID);
        assertNotNull(result);
    }
    @Test
    void getMerchantList(){
        MerchantListDTO dto = new MerchantListDTO();
        Mockito.when(merchantService.getMerchantList(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(dto);

        MerchantListDTO result = merchantService.getMerchantList(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID, null);
        assertNotNull(result);
    }

    @Test
    void getMerchantInitiativeList() {
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
    void getMerchantInitiativeList_notFound() {
        when(merchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());

        assertNull(merchantService.getMerchantInitiativeList(MERCHANT_ID));
    }
}