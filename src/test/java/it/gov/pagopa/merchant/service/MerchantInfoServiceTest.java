package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantInfoDTO;
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
class MerchantInfoServiceTest {

    @Mock private MerchantRepository merchantRepositoryMock;

    private MerchantInfoService merchantInfoService;

    @BeforeEach
    void setUp() {
        merchantInfoService = new MerchantInfoServiceImpl(merchantRepositoryMock);
    }

    @Test
    void retrieveMerchantId(){
        // When
        Merchant merchant = MerchantFaker.mockInstance(1);

        doReturn(Optional.of(merchant)).when(merchantRepositoryMock)
                .findByFiscalCodeAndAcquirerId(merchant.getFiscalCode(), merchant.getAcquirerId());

        MerchantInfoDTO merchantIdOkResult = merchantInfoService.getMerchantInfo(merchant.getFiscalCode(), merchant.getAcquirerId());

        assertNotNull(merchantIdOkResult);
        assertEquals(merchant.getMerchantId(), merchantIdOkResult.getMerchantId());
    }

    @Test
    void retrieveMerchantId_NotFoundException(){

        doReturn(Optional.empty()).when(merchantRepositoryMock)
                .findByFiscalCodeAndAcquirerId(Mockito.eq("DUMMYFISCALCODE"), Mockito.any());

        ClientExceptionWithBody clientExceptionWithBody = assertThrows(ClientExceptionWithBody.class,
                () -> merchantInfoService.getMerchantInfo("DUMMYFISCALCODE", "DUMMYACQUIRERID"));

        assertEquals(HttpStatus.NOT_FOUND, clientExceptionWithBody.getHttpStatus());
        assertEquals(MerchantConstants.NOT_FOUND, clientExceptionWithBody.getCode());
        assertEquals(String.format(MerchantConstants.INITIATIVE_AND_MERCHANT_NOT_FOUND, "DUMMYFISCALCODE", "DUMMYACQUIRERID"), clientExceptionWithBody.getMessage());
    }
}
