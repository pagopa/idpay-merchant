package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleReferentPatchDTO;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleDuplicateException;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.KeycloakService;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyBoolean;

@ExtendWith(MockitoExtension.class)
class UpdatePointOfSaleReferentServiceTest {

  @Mock
  private PointOfSaleRepository pointOfSaleRepository;
  @Mock
  private PointOfSaleFinderService pointOfSaleFinderService;
  @Mock
  private KeycloakService keycloakService;
  @Mock
  private PointOfSaleValidator pointOfSaleValidator;

  private UpdatePointOfSaleReferentService service;

  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String POINT_OF_SALE_ID = "POS_ID";

  @BeforeEach
  void setUp() {
    service = new UpdatePointOfSaleReferentServiceImpl(
        pointOfSaleRepository, pointOfSaleFinderService, keycloakService, pointOfSaleValidator);
  }

  @Test
  void updateReferent_emailChanged_updatesPointOfSaleAndSendsResetEmail() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(POINT_OF_SALE_ID);
    pointOfSale.setMerchantId(MERCHANT_ID);
    pointOfSale.setContactEmail("old.email@example.com");

    PointOfSaleReferentPatchDTO patchDTO = PointOfSaleReferentPatchDTO.builder()
        .contactEmail("NEW.Email@Example.com ")
        .contactName(" NewName ")
        .contactSurname(" NewSurname ")
        .build();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
        .thenReturn(pointOfSale);
    when(pointOfSaleRepository.findByContactEmail("new.email@example.com"))
        .thenReturn(Optional.empty());
    when(pointOfSaleRepository.save(pointOfSale)).thenReturn(pointOfSale);

    PointOfSale result = service.updateReferent(MERCHANT_ID, POINT_OF_SALE_ID, patchDTO);

    verify(pointOfSaleValidator).validateEmailFormat("new.email@example.com");
    assertEquals("new.email@example.com", result.getContactEmail());
    assertEquals("NewName", result.getContactName());
    assertEquals("NewSurname", result.getContactSurname());
    verify(keycloakService).updateReferentUserOnKeycloak(
        pointOfSale, "old.email@example.com", true);
  }

  @Test
  void updateReferent_emailUnchanged_updatesPointOfSaleWithoutSendingResetEmail() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(POINT_OF_SALE_ID);
    pointOfSale.setContactEmail("same.email@example.com");

    PointOfSaleReferentPatchDTO patchDTO = PointOfSaleReferentPatchDTO.builder()
        .contactEmail("SAME.Email@example.com")
        .contactName("Mario")
        .contactSurname("Rossi")
        .build();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
        .thenReturn(pointOfSale);
    when(pointOfSaleRepository.findByContactEmail("same.email@example.com"))
        .thenReturn(Optional.of(pointOfSale));
    when(pointOfSaleRepository.save(pointOfSale)).thenReturn(pointOfSale);

    service.updateReferent(MERCHANT_ID, POINT_OF_SALE_ID, patchDTO);

    ArgumentCaptor<PointOfSale> pointOfSaleCaptor = ArgumentCaptor.forClass(PointOfSale.class);
    verify(pointOfSaleRepository).save(pointOfSaleCaptor.capture());
    assertEquals("Mario", pointOfSaleCaptor.getValue().getContactName());
    assertEquals("Rossi", pointOfSaleCaptor.getValue().getContactSurname());
    verify(keycloakService).updateReferentUserOnKeycloak(
        pointOfSale, "same.email@example.com", false);
  }

  @Test
  void updateReferent_emailMissing_keepsExistingEmailAndUpdatesNameSurnameWithoutResetEmail() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(POINT_OF_SALE_ID);
    pointOfSale.setContactEmail("same.email@example.com");

    PointOfSaleReferentPatchDTO patchDTO = PointOfSaleReferentPatchDTO.builder()
        .contactName("Mario")
        .contactSurname("Rossi")
        .build();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
        .thenReturn(pointOfSale);
    when(pointOfSaleRepository.save(pointOfSale)).thenReturn(pointOfSale);

    service.updateReferent(MERCHANT_ID, POINT_OF_SALE_ID, patchDTO);

    ArgumentCaptor<PointOfSale> pointOfSaleCaptor = ArgumentCaptor.forClass(PointOfSale.class);
    verify(pointOfSaleRepository).save(pointOfSaleCaptor.capture());
    assertEquals("same.email@example.com", pointOfSaleCaptor.getValue().getContactEmail());
    assertEquals("Mario", pointOfSaleCaptor.getValue().getContactName());
    assertEquals("Rossi", pointOfSaleCaptor.getValue().getContactSurname());
    verify(pointOfSaleValidator, never()).validateEmailFormat(any());
    verify(pointOfSaleRepository, never()).findByContactEmail(any());
    verify(keycloakService).updateReferentUserOnKeycloak(
        pointOfSale, "same.email@example.com", false);
  }

  @Test
  void updateReferent_onlyNameProvided_keepsExistingEmailAndSurname() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(POINT_OF_SALE_ID);
    pointOfSale.setContactEmail("same.email@example.com");
    pointOfSale.setContactName("OldName");
    pointOfSale.setContactSurname("OldSurname");

    PointOfSaleReferentPatchDTO patchDTO = PointOfSaleReferentPatchDTO.builder()
        .contactName("NewName")
        .build();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
        .thenReturn(pointOfSale);
    when(pointOfSaleRepository.save(pointOfSale)).thenReturn(pointOfSale);

    service.updateReferent(MERCHANT_ID, POINT_OF_SALE_ID, patchDTO);

    ArgumentCaptor<PointOfSale> pointOfSaleCaptor = ArgumentCaptor.forClass(PointOfSale.class);
    verify(pointOfSaleRepository).save(pointOfSaleCaptor.capture());
    assertEquals("same.email@example.com", pointOfSaleCaptor.getValue().getContactEmail());
    assertEquals("NewName", pointOfSaleCaptor.getValue().getContactName());
    assertEquals("OldSurname", pointOfSaleCaptor.getValue().getContactSurname());
    verify(keycloakService).updateReferentUserOnKeycloak(
        pointOfSale, "same.email@example.com", false);
  }

  @Test
  void updateReferent_onlySurnameProvided_keepsExistingEmailAndName() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(POINT_OF_SALE_ID);
    pointOfSale.setContactEmail("same.email@example.com");
    pointOfSale.setContactName("OldName");
    pointOfSale.setContactSurname("OldSurname");

    PointOfSaleReferentPatchDTO patchDTO = PointOfSaleReferentPatchDTO.builder()
        .contactSurname("NewSurname")
        .build();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
        .thenReturn(pointOfSale);
    when(pointOfSaleRepository.save(pointOfSale)).thenReturn(pointOfSale);

    service.updateReferent(MERCHANT_ID, POINT_OF_SALE_ID, patchDTO);

    ArgumentCaptor<PointOfSale> pointOfSaleCaptor = ArgumentCaptor.forClass(PointOfSale.class);
    verify(pointOfSaleRepository).save(pointOfSaleCaptor.capture());
    assertEquals("same.email@example.com", pointOfSaleCaptor.getValue().getContactEmail());
    assertEquals("OldName", pointOfSaleCaptor.getValue().getContactName());
    assertEquals("NewSurname", pointOfSaleCaptor.getValue().getContactSurname());
    verify(keycloakService).updateReferentUserOnKeycloak(
        pointOfSale, "same.email@example.com", false);
  }

  @Test
  void updateReferent_blankName_throwsBadRequestBeforeSaving() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(POINT_OF_SALE_ID);
    pointOfSale.setContactEmail("old.email@example.com");

    PointOfSaleReferentPatchDTO patchDTO = PointOfSaleReferentPatchDTO.builder()
        .contactName(" ")
        .build();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
        .thenReturn(pointOfSale);

    Assertions.assertThrows(ClientExceptionWithBody.class,
        () -> service.updateReferent(MERCHANT_ID, POINT_OF_SALE_ID, patchDTO));

    verify(pointOfSaleRepository, never()).save(any());
    verify(keycloakService, never()).updateReferentUserOnKeycloak(any(), any(), anyBoolean());
  }

  @Test
  void updateReferent_blankEmail_throwsBadRequestBeforeSaving() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(POINT_OF_SALE_ID);
    pointOfSale.setContactEmail("old.email@example.com");

    PointOfSaleReferentPatchDTO patchDTO = PointOfSaleReferentPatchDTO.builder()
        .contactEmail(" ")
        .contactName("Mario")
        .contactSurname("Rossi")
        .build();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
        .thenReturn(pointOfSale);

    Assertions.assertThrows(ClientExceptionWithBody.class,
        () -> service.updateReferent(MERCHANT_ID, POINT_OF_SALE_ID, patchDTO));

    verify(pointOfSaleValidator, never()).validateEmailFormat(any());
    verify(pointOfSaleRepository, never()).findByContactEmail(any());
    verify(pointOfSaleRepository, never()).save(any());
    verify(keycloakService, never()).updateReferentUserOnKeycloak(any(), any(), anyBoolean());
  }

  @Test
  void updateReferent_emailAlreadyUsedByAnotherPointOfSale_throwsDuplicateException() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(POINT_OF_SALE_ID);
    pointOfSale.setContactEmail("old.email@example.com");

    PointOfSale anotherPointOfSale = PointOfSaleFaker.mockInstance();
    anotherPointOfSale.setId("ANOTHER_POS_ID");
    anotherPointOfSale.setContactEmail("used.email@example.com");

    PointOfSaleReferentPatchDTO patchDTO = PointOfSaleReferentPatchDTO.builder()
        .contactEmail("used.email@example.com")
        .contactName("Mario")
        .contactSurname("Rossi")
        .build();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
        .thenReturn(pointOfSale);
    when(pointOfSaleRepository.findByContactEmail("used.email@example.com"))
        .thenReturn(Optional.of(anotherPointOfSale));

    Assertions.assertThrows(PointOfSaleDuplicateException.class,
        () -> service.updateReferent(MERCHANT_ID, POINT_OF_SALE_ID, patchDTO));

    verify(pointOfSaleRepository, never()).save(any());
    verify(keycloakService, never()).updateReferentUserOnKeycloak(any(), any(), anyBoolean());
  }

  @Test
  void updateReferent_invalidNormalizedEmail_throwsBadRequestBeforeSaving() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(POINT_OF_SALE_ID);
    pointOfSale.setContactEmail("old.email@example.com");

    PointOfSaleReferentPatchDTO patchDTO = PointOfSaleReferentPatchDTO.builder()
        .contactEmail("referent@domain_with_underscore.it")
        .contactName("Mario")
        .contactSurname("Rossi")
        .build();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
        .thenReturn(pointOfSale);
    org.mockito.Mockito.doThrow(new ClientExceptionWithBody(
            org.springframework.http.HttpStatus.BAD_REQUEST,
            "POINT_OF_SALE_INVALID_FORMAT",
            "Email must be a valid email address."))
        .when(pointOfSaleValidator).validateEmailFormat("referent@domain_with_underscore.it");

    Assertions.assertThrows(ClientExceptionWithBody.class,
        () -> service.updateReferent(MERCHANT_ID, POINT_OF_SALE_ID, patchDTO));

    verify(pointOfSaleRepository, never()).findByContactEmail(any());
    verify(pointOfSaleRepository, never()).save(any());
    verify(keycloakService, never()).updateReferentUserOnKeycloak(any(), any(), anyBoolean());
  }
}
