package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.faker.MerchantFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MerchantIdServiceTest {

    @Mock private MerchantRepository merchantRepositoryMock;

    private MerchantIdService merchantIdService;

    @BeforeEach
    void setUp() {
        merchantIdService = new MerchantIdServiceImpl(merchantRepositoryMock);
    }

    @Test
    void retrieveMerchantId(){
        // When
        Merchant merchant = MerchantFaker.mockInstance(1);

        doReturn(Optional.of(merchant)).when(merchantRepositoryMock)
                .findByFiscalCodeAndAcquirerId(merchant.getAcquirerId(), merchant.getFiscalCode());

        String merchantIdOkResult = merchantIdService.getMerchantInfo(merchant.getAcquirerId(), merchant.getFiscalCode());

        assertNotNull(merchantIdOkResult);
        assertEquals(merchant.getMerchantId(), merchantIdOkResult);
    }

    @Test
    void retrieveMerchantId_NotFoundException(){

        doReturn(Optional.empty()).when(merchantRepositoryMock)
                .findByFiscalCodeAndAcquirerId(Mockito.any(), Mockito.eq("DUMMYFISCALCODE"));

        ClientExceptionWithBody clientExceptionWithBody = assertThrows(ClientExceptionWithBody.class,
                () -> merchantIdService.getMerchantInfo("DUMMYACQUIRERID", "DUMMYFISCALCODE"));

        assertEquals(HttpStatus.NOT_FOUND, clientExceptionWithBody.getHttpStatus());
        assertEquals(MerchantConstants.NOT_FOUND, clientExceptionWithBody.getCode());
        assertEquals(String.format(MerchantConstants.MERCHANTID_BY_ACQUIRERID_AND_FISCALCODE_MESSAGE,"DUMMYACQUIRERID" , "DUMMYFISCALCODE"), clientExceptionWithBody.getMessage());
    }
}
