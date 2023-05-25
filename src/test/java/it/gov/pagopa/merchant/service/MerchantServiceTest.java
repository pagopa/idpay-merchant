package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.merchant.MerchantListService;
import it.gov.pagopa.merchant.service.merchant.UploadingMerchantService;
import it.gov.pagopa.merchant.test.faker.MerchantFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepositoryMock;
    @Mock
    private MerchantDetailService merchantDetailServiceMock;
    @Mock
    private MerchantListService merchantListServiceMock;
    @Mock
    private UploadingMerchantService uploadingMerchantServiceMock;

    private MerchantService merchantService;

    @BeforeEach
    void setUp() {
        merchantService = new MerchantServiceImpl(merchantDetailServiceMock,merchantListServiceMock,merchantRepositoryMock, uploadingMerchantServiceMock);
    }

    @Test
    void retrieveMerchantId(){
        // When
        Merchant merchant = MerchantFaker.mockInstance(1);

        doReturn(Optional.of(merchant)).when(merchantRepositoryMock)
                .findByAcquirerIdAndFiscalCode(merchant.getAcquirerId(), merchant.getFiscalCode());

        String merchantIdOkResult = merchantService.retrieveMerchantId(merchant.getAcquirerId(), merchant.getFiscalCode());

        assertNotNull(merchantIdOkResult);
        assertEquals(merchant.getMerchantId(), merchantIdOkResult);
    }

    @Test
    void retrieveMerchantId_NotFound(){

        doReturn(Optional.empty()).when(merchantRepositoryMock)
                .findByAcquirerIdAndFiscalCode(Mockito.any(), Mockito.eq("DUMMYFISCALCODE"));

        String merchantIdNotFoundResult= merchantService.retrieveMerchantId("DUMMYACQUIRERID", "DUMMYFISCALCODE");

        assertNull(merchantIdNotFoundResult);
        Mockito.verify(merchantRepositoryMock).findByAcquirerIdAndFiscalCode(Mockito.anyString(), Mockito.anyString());
    }
}
