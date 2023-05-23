package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.exception.ClientException;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantInitiativesServiceTest {

    public static final String MERCHANT_ID = "MERCHANT_ID";
    @Mock
    private MerchantRepository merchantRepository;

    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper = new Initiative2InitiativeDTOMapper();
    MerchantInitiativesService service;

    @BeforeEach
    void setUp() {
        service = new MerchantInitiativesServiceImpl(
                merchantRepository,
                initiative2InitiativeDTOMapper);
    }

    @Test
    void getMerchantInitiativeList() {
        Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
                .initiativeList(List.of(
                        InitiativeFaker.mockInstance(1),
                        InitiativeFaker.mockInstance(2)))
                .build();

        when(merchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(merchant));

        List<InitiativeDTO> result = service.getMerchantInitiativeList(MERCHANT_ID);

        assertEquals(
                merchant.getInitiativeList().stream()
                        .map(initiative2InitiativeDTOMapper)
                        .toList(),
                result);
    }

    @Test
    void getMerchantInitiativeList_notFound() {
        when(merchantRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());

        ClientException result = assertThrows(ClientException.class,
                () -> service.getMerchantInitiativeList(MERCHANT_ID));
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals(MerchantConstants.NOT_FOUND, ((ClientExceptionWithBody)result).getCode());
        assertEquals(String.format(MerchantConstants.MERCHANT_BY_MERCHANT_ID_MESSAGE, MERCHANT_ID),
                result.getMessage());
    }
}
