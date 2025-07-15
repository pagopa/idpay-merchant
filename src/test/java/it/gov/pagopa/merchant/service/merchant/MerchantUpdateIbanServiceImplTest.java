package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantIbanPatchDTO;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
  private static final String VALID_IBAN = "IT60X0542811101000000123456";
  private static final String VALID_HOLDER = "John Doe";
  private static final String INVALID_IBAN = "IT12345";
  private static final String INVALID_HOLDER = "John Doe 123";

  @BeforeEach
  void setUp() {
    service = new MerchantUpdateIbanServiceImpl(merchantRepositoryMock, merchantDetailServiceMock);
  }

  private Merchant buildMerchant() {
    Initiative initiative = InitiativeFaker.mockInstanceBuilder(1)
        .initiativeId(INITIATIVE_ID)
        .organizationId(ORGANIZATION_ID)
        .build();
    return MerchantFaker.mockInstanceBuilder(1)
        .merchantId(MERCHANT_ID)
        .iban(null)
        .ibanHolder(null)
        .initiativeList(List.of(initiative))
        .build();
  }

  @Test
  void updateIban_success_onlyIban() {
    // Given
    Merchant merchant = buildMerchant();
    MerchantIbanPatchDTO patchDTO = new MerchantIbanPatchDTO(VALID_IBAN, null);
    MerchantDetailDTO expectedDetailDTO = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
    when(merchantDetailServiceMock.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID)).thenReturn(expectedDetailDTO);

    // When
    MerchantDetailDTO result = service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, patchDTO);

    // Then
    assertNotNull(result);
    assertEquals(expectedDetailDTO, result);

    ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
    verify(merchantRepositoryMock).save(merchantCaptor.capture());

    Merchant savedMerchant = merchantCaptor.getValue();
    assertEquals(VALID_IBAN, savedMerchant.getIban());
    assertNull(savedMerchant.getIbanHolder());
  }

  @Test
  void updateIban_success_onlyIbanHolder() {
    // Given
    Merchant merchant = buildMerchant();
    MerchantIbanPatchDTO patchDTO = new MerchantIbanPatchDTO(null, VALID_HOLDER);
    MerchantDetailDTO expectedDetailDTO = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
    when(merchantDetailServiceMock.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID)).thenReturn(expectedDetailDTO);

    // When
    MerchantDetailDTO result = service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, patchDTO);

    // Then
    assertNotNull(result);
    assertEquals(expectedDetailDTO, result);

    ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
    verify(merchantRepositoryMock).save(merchantCaptor.capture());

    Merchant savedMerchant = merchantCaptor.getValue();
    assertNull(savedMerchant.getIban());
    assertEquals(VALID_HOLDER, savedMerchant.getIbanHolder());
  }

  @Test
  void updateIban_success_bothFields() {
    // Given
    Merchant merchant = buildMerchant();
    MerchantIbanPatchDTO patchDTO = new MerchantIbanPatchDTO(VALID_IBAN, VALID_HOLDER);
    MerchantDetailDTO expectedDetailDTO = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
    when(merchantDetailServiceMock.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID)).thenReturn(expectedDetailDTO);

    // When
    MerchantDetailDTO result = service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, patchDTO);

    // Then
    assertNotNull(result);
    assertEquals(expectedDetailDTO, result);

    ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
    verify(merchantRepositoryMock).save(merchantCaptor.capture());

    Merchant savedMerchant = merchantCaptor.getValue();
    assertEquals(VALID_IBAN, savedMerchant.getIban());
    assertEquals(VALID_HOLDER, savedMerchant.getIbanHolder());
  }

  @Test
  void updateIban_fail_merchantNotFound() {
    // Given
    MerchantIbanPatchDTO patchDTO = new MerchantIbanPatchDTO(VALID_IBAN, VALID_HOLDER);
    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.empty());

    // When & Then
    MerchantNotFoundException e = assertThrows(MerchantNotFoundException.class,
        () -> service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, patchDTO));
    assertEquals(String.format("Merchant with id %s not found.", MERCHANT_ID), e.getMessage());
    verify(merchantRepositoryMock, never()).save(any());
  }

  @Test
  void updateIban_fail_initiativeNotFoundForMerchant() {
    // Given
    MerchantIbanPatchDTO patchDTO = new MerchantIbanPatchDTO(VALID_IBAN, VALID_HOLDER);
    Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
        .merchantId(MERCHANT_ID)
        .initiativeList(Collections.emptyList()) // No initiatives
        .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    // When & Then
    MerchantNotFoundException e = assertThrows(MerchantNotFoundException.class,
        () -> service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, patchDTO));
    assertEquals(String.format("Merchant with id %s is not associated with initiative %s for organization %s.",
        MERCHANT_ID, INITIATIVE_ID, ORGANIZATION_ID), e.getMessage());
    verify(merchantRepositoryMock, never()).save(any());
  }

  @Test
  void updateIban_fail_ibanAlreadySet() {
    // Given
    Merchant merchant = buildMerchant();
    merchant.setIban("IT00A1234567890123456789012"); // Pre-existing IBAN
    MerchantIbanPatchDTO patchDTO = new MerchantIbanPatchDTO(VALID_IBAN, null);

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    // When & Then
    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, patchDTO));
    assertEquals("Invalid state of merchant, IBAN field is not empty", e.getMessage());
    verify(merchantRepositoryMock, never()).save(any());
  }

  @Test
  void updateIban_fail_ibanHolderAlreadySet() {
    // Given
    Merchant merchant = buildMerchant();
    merchant.setIbanHolder("Old Holder"); // Pre-existing IBAN Holder
    MerchantIbanPatchDTO patchDTO = new MerchantIbanPatchDTO(null, VALID_HOLDER);

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    // When & Then
    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, patchDTO));
    assertEquals("Invalid state of merchant, IBAN Holder field is not empty", e.getMessage());
    verify(merchantRepositoryMock, never()).save(any());
  }

  @Test
  void updateIban_fail_invalidIbanFormat() {
    // Given
    Merchant merchant = buildMerchant();
    MerchantIbanPatchDTO patchDTO = new MerchantIbanPatchDTO(INVALID_IBAN, null);

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    // When & Then
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
        () -> service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, patchDTO));
    assertEquals("Invalid IBAN format.", e.getMessage());
    verify(merchantRepositoryMock, never()).save(any());
  }

  @Test
  void updateIban_fail_invalidIbanHolderFormat() {
    // Given
    Merchant merchant = buildMerchant();
    MerchantIbanPatchDTO patchDTO = new MerchantIbanPatchDTO(null, INVALID_HOLDER);

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    // When & Then
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
        () -> service.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, patchDTO));
    assertEquals("Invalid IBAN holder format.", e.getMessage());
    verify(merchantRepositoryMock, never()).save(any());
  }
}