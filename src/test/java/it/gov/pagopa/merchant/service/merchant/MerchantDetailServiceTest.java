package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.common.utils.TestUtils;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.mapper.MerchantModelToDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantDetailServiceTest {

  @Mock private MerchantRepository repositoryMock;
  @Mock private final MerchantModelToDTOMapper merchantModelToDTOMapperMock = new MerchantModelToDTOMapper();
  private final String INITIATIVE_ID = "INITIATIVE_ID";
  private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
  private final String MERCHANT_ID = "MERCHANT_ID";

  MerchantDetailService service;

  @BeforeEach
  void setUp() {
    service = new MerchantDetailServiceImpl(
            repositoryMock,
            merchantModelToDTOMapperMock);
  }

  @Test
  void getMerchantDetail() {
    MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
    Merchant merchant = MerchantFaker.mockInstance(1);

    when(repositoryMock.retrieveByInitiativeIdAndOrganizationIdAndMerchantId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(Optional.of(merchant));
    when(merchantModelToDTOMapperMock.toMerchantDetailDTO(Mockito.any(), Mockito.anyString())).thenReturn(dto);

    MerchantDetailDTO result = service.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID);

    assertEquals(dto, result);
    TestUtils.checkNotNullFields(result);
    assertEquals(dto.getInitiativeId(), result.getInitiativeId());
  }

  @Test
  void getMerchantDetail_notFound() {
    when(repositoryMock.retrieveByInitiativeIdAndOrganizationIdAndMerchantId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.empty());

    MerchantNotFoundException result = assertThrows(MerchantNotFoundException.class,
            () -> service.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID));
    assertEquals(ExceptionCode.MERCHANT_NOT_ONBOARDED, result.getCode());
    assertEquals(String.format(ExceptionMessage.INITIATIVE_AND_MERCHANT_NOT_FOUND, INITIATIVE_ID),
            result.getMessage());
  }

  @Test
  void getMerchantDetailByMerchantIdAndInitiativeId() {
    Merchant merchant = MerchantFaker.mockInstance(1);
    MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);

    when(repositoryMock.retrieveByMerchantIdAndInitiativeId(MERCHANT_ID, INITIATIVE_ID))
            .thenReturn(Optional.of(merchant));
    when(merchantModelToDTOMapperMock.toMerchantDetailDTO(merchant, INITIATIVE_ID)).thenReturn(merchantDetailDTO);

    MerchantDetailDTO result = service.getMerchantDetail(MERCHANT_ID, INITIATIVE_ID);

    assertNotNull(result);
    assertEquals(merchantDetailDTO, result);
    TestUtils.checkNotNullFields(result);
    assertEquals(merchantDetailDTO.getInitiativeId(), result.getInitiativeId());
  }

  @Test
  void getMerchantDetailByMerchantIdAndInitiativeId_NotFound() {
    when(repositoryMock.retrieveByMerchantIdAndInitiativeId(MERCHANT_ID, INITIATIVE_ID))
            .thenReturn(Optional.empty());

    MerchantDetailDTO result = service.getMerchantDetail(MERCHANT_ID, INITIATIVE_ID);

    assertNull(result);
  }


  @Test
  void getMerchantDetailByMerchantId_found() {
    Merchant merchant = MerchantFaker.mockInstance(1);

    when(repositoryMock.findById(MERCHANT_ID))
            .thenReturn(Optional.of(merchant));
    MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantModelToDTOMapperMock.toMerchantDetailDTOWithoutInitiative(any())).thenReturn(merchantDetailDTO);

    MerchantDetailDTO result = service.getMerchantDetail(MERCHANT_ID);

    assertNotNull(result);
  }

  @Test
  void getMerchantDetailByMerchantId_notFound() {

    when(repositoryMock.findById(MERCHANT_ID))
            .thenReturn(Optional.empty());

    MerchantDetailDTO result = service.getMerchantDetail(MERCHANT_ID);

    assertNull(result);
  }

}
