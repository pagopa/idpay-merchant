package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.common.utils.TestUtils;
import it.gov.pagopa.merchant.dto.IbanPutDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantUpdateIbanServiceImplTest {

  @Mock
  private MerchantRepository merchantRepositoryMock;
  @Mock
  private MerchantDetailService merchantDetailServiceMock;

  private MerchantUpdateIbanService service;

  private static final String MERCHANT_ID = "MERCHANT_ID_1";
  private static final String ORGANIZATION_ID = "ORGANIZATION_ID_1";
  private static final String INITIATIVE_ID = "INITIATIVE_ID_1";
  private static final String NEW_IBAN = "IT60X0542811101000000123456";
  private static final String NEW_HOLDER = "NEW HOLDER";

  @BeforeEach
  void setUp() {
    service = new MerchantUpdateIbanServiceImpl(merchantRepositoryMock, merchantDetailServiceMock);
  }

  @Test
  void updateIban_success_allFields() {
    // Given
    IbanPutDTO ibanPutDTO = new IbanPutDTO(NEW_IBAN, NEW_HOLDER);
    Initiative initiative = InitiativeFaker.mockInstanceBuilder(1)
        .initiativeId(INITIATIVE_ID)
        .organizationId(ORGANIZATION_ID)
        .build();
    Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
        .merchantId(MERCHANT_ID)
        .initiativeList(List.of(initiative))
        .iban("OLD_IBAN")
        .holder("OLD_HOLDER")
        .build();
    MerchantDetailDTO expectedDto = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
    when(merchantDetailServiceMock.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID)).thenReturn(expectedDto);

    // When
    MerchantDetailDTO result = service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, ibanPutDTO);

    // Then
    assertNotNull(result);
    assertEquals(expectedDto, result);
    TestUtils.checkNotNullFields(result);

    ArgumentCaptor<Merchant> captor = ArgumentCaptor.forClass(Merchant.class);
    Mockito.verify(merchantRepositoryMock).save(captor.capture());
    Merchant savedMerchant = captor.getValue();
    assertEquals(NEW_IBAN, savedMerchant.getIban());
    assertEquals(NEW_HOLDER, savedMerchant.getHolder());
  }

  @Test
  void updateIban_success_onlyIban() {
    // Given
    IbanPutDTO ibanPutDTO = new IbanPutDTO(NEW_IBAN, null);
    Initiative initiative = InitiativeFaker.mockInstanceBuilder(1)
        .initiativeId(INITIATIVE_ID)
        .organizationId(ORGANIZATION_ID)
        .build();
    Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
        .merchantId(MERCHANT_ID)
        .initiativeList(List.of(initiative))
        .iban("OLD_IBAN")
        .holder("OLD_HOLDER")
        .build();
    MerchantDetailDTO expectedDto = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
    when(merchantDetailServiceMock.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID)).thenReturn(expectedDto);

    // When
    service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, ibanPutDTO);

    // Then
    ArgumentCaptor<Merchant> captor = ArgumentCaptor.forClass(Merchant.class);
    Mockito.verify(merchantRepositoryMock).save(captor.capture());
    Merchant savedMerchant = captor.getValue();
    assertEquals(NEW_IBAN, savedMerchant.getIban());
    assertEquals("OLD_HOLDER", savedMerchant.getHolder()); // Holder should not change
  }

  @Test
  void updateIban_merchantNotFound() {
    // Given
    IbanPutDTO ibanPutDTO = new IbanPutDTO(NEW_IBAN, NEW_HOLDER);
    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.empty());

    // When & Then
    MerchantNotFoundException e = assertThrows(MerchantNotFoundException.class,
        () -> service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, ibanPutDTO));
    assertEquals(String.format("Merchant with id %s not found.", MERCHANT_ID), e.getMessage());
  }

  @Test
  void updateIban_initiativeNotFoundForMerchant() {
    // Given
    IbanPutDTO ibanPutDTO = new IbanPutDTO(NEW_IBAN, NEW_HOLDER);
    Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
        .merchantId(MERCHANT_ID)
        .initiativeList(Collections.emptyList()) // No initiatives
        .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    // When & Then
    MerchantNotFoundException e = assertThrows(MerchantNotFoundException.class,
        () -> service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, ibanPutDTO));
    assertEquals(String.format("Merchant with id %s is not associated with initiative %s for organization %s.",
        MERCHANT_ID, INITIATIVE_ID, ORGANIZATION_ID), e.getMessage());
  }
}