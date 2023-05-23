package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantInfoDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.merchant.MerchantListService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {
    @Mock
    private MerchantDetailService merchantDetailServiceMock;
    @Mock
    private MerchantListService merchantListServiceMock;
    @Mock
    private RetrieveMerchantIdService retrieveMerchantIdServiceMock;

    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String MERCHANT_FISCALCODE = "MERCHANT_FISCALCODE";
    private static final String MERCHANT_ACQUIRERID = "MERCHANT_ACQUIRERID";

    private MerchantServiceImpl merchantService;

    @BeforeEach
    void setUp(){
        merchantService = new MerchantServiceImpl(
                merchantDetailServiceMock,
                merchantListServiceMock,
                retrieveMerchantIdServiceMock);
    }

    @AfterEach
    void verifyNoMoreMockInteractions() {
        Mockito.verifyNoMoreInteractions(
                merchantDetailServiceMock,
                merchantListServiceMock,
                retrieveMerchantIdServiceMock);
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
    void retrieveMerchantId(){
        MerchantInfoDTO merchantInfoDTO = new MerchantInfoDTO();

        Mockito.when(merchantService.retrieveMerchantId(Mockito.anyString(),Mockito.anyString())).thenReturn(merchantInfoDTO);

        MerchantInfoDTO result = merchantService.retrieveMerchantId(MERCHANT_FISCALCODE,MERCHANT_ACQUIRERID);

        assertNotNull(result);
    }
}