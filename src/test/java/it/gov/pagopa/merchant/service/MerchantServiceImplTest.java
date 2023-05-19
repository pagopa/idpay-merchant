package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.exception.MerchantException;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
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

    @Mock private MerchantRepository merchantRepositoryMock;

    private MerchantServiceImpl merchantService;

    @BeforeEach
    void setUp() {
        merchantService = new MerchantServiceImpl(merchantRepositoryMock);
    }

    @Test
    void retrieveMerchantId(){
        // When
        Merchant merchant = MerchantFaker.mockInstance(1);

        doReturn(Optional.empty()).when(merchantRepositoryMock)
                .findByFiscalCodeAndAcquirerId(Mockito.eq("DUMMYFISCALCODE"), Mockito.any());

        doReturn(Optional.of(merchant)).when(merchantRepositoryMock)
                .findByFiscalCodeAndAcquirerId(merchant.getFiscalCode(), merchant.getAcquirerId());

        //region use case OK
        String merchantIdOkResult = merchantService.retrieveMerchantId(merchant.getFiscalCode(), merchant.getAcquirerId());
        assertEquals(merchant.getMerchantId(), merchantIdOkResult);
        //endregion

        //region use case KO
        try {
            merchantService.retrieveMerchantId("DUMMYFISCALCODE", "AQUIRERID1");
            fail();
        } catch (RuntimeException e) {
            assertTrue(e instanceof MerchantException);
        }
        //endregion
    }
}
