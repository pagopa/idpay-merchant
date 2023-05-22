package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.ClientException;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.mapper.MerchantModelToDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.merchant.test.utils.TestUtils;
import it.gov.pagopa.merchant.utils.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantDetailServiceTest {

  @Mock private MerchantRepository repositoryMock;
  @Mock private Utilities utilitiesMock;
  @Mock private final MerchantModelToDTOMapper merchantModelToDTOMapperMock = new MerchantModelToDTOMapper();
  private final String INITIATIVE_ID = "INITIATIVE_ID";
  private final String MERCHANT_ID = "MERCHANT_ID";

  MerchantDetailService service;

  @BeforeEach
  void setUp() {
    service = new MerchantDetailServiceImpl(
            repositoryMock,
            merchantModelToDTOMapperMock,
            utilitiesMock);
  }

  @Test
  void getMerchantDetail() {
    MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
    Merchant merchant = MerchantFaker.mockInstance(1);

    when(repositoryMock.retrieveByInitiativeIdAndMerchantId(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(Optional.of(merchant));
    when(merchantModelToDTOMapperMock.toMerchantDetailDTO(Mockito.any(), Mockito.anyString())).thenReturn(dto);

    MerchantDetailDTO result = service.getMerchantDetail(INITIATIVE_ID, MERCHANT_ID);

    assertEquals(dto, result);
    TestUtils.checkNotNullFields(result);
    assertEquals(dto.getInitiativeId(), result.getInitiativeId());
  }

  @Test
  void getMerchantDetail_notFound() {
    when(repositoryMock.retrieveByInitiativeIdAndMerchantId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.empty());

    ClientException result = assertThrows(ClientException.class,
            () -> service.getMerchantDetail(INITIATIVE_ID, MERCHANT_ID));
    assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
    assertEquals(MerchantConstants.NOT_FOUND, ((ClientExceptionWithBody) result).getCode());
    assertEquals(String.format(MerchantConstants.INITIATIVE_AND_MERCHANT_NOT_FOUND, INITIATIVE_ID, MERCHANT_ID),
            result.getMessage());
  }
}
