package it.gov.pagopa.merchant.service;

import com.mongodb.MongoException;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestClient;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.connector.pdnd.PdndInfoCamereConnectorImpl;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.*;
import it.gov.pagopa.merchant.dto.initiative.InitiativeResponse;
import it.gov.pagopa.merchant.dto.pdnd.PageResponse;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.mapper.MerchantCreateDTOMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.merchant.*;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantUpdateDTOFaker;
import it.gov.pagopa.merchant.utils.Utilities;
import it.gov.pagopa.merchant.utils.validator.MerchantValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {

  @Mock
  private MerchantDetailService merchantDetailServiceMock;
  @Mock
  private MerchantListService merchantListServiceMock;
  @Mock
  private MerchantProcessOperationService merchantProcessOperationService;
  @Mock
  private MerchantUpdatingInitiativeService merchantUpdatingInitiativeService;
  @Mock
  private MerchantUpdateIbanService merchantUpdateIbanService;
  @Mock
  private MerchantRepository merchantRepositoryMock;
  @Mock
  private UploadingMerchantService uploadingMerchantServiceMock;
  @Mock
  private InitiativeRestConnector initiativeRestConnector;
  @Mock
  private PointOfSaleRepository pointOfSaleRepositoryMock;
  @Mock
  private MerchantValidator merchantValidatorMock;
  @Mock
  private Keycloak keycloakAdminClientMock;
  @Mock
  private PdndInfoCamereConnectorImpl pdndConnectorMock;
  @Mock
  private InitiativeRestClient initiativeRestClientMock;

  private MerchantServiceImpl merchantService;

  private static final String REALM = "test-realm";
  private static final String INITIATIVE_ID = "INITIATIVE_ID";
  private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
  private static final String ACQUIRER_ID = "PAGOPA";
  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String OPERATION_TYPE_DELETE_INITIATIVE = "DELETE_INITIATIVE";
  private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper = new Initiative2InitiativeDTOMapper();
  private final MerchantCreateDTOMapper merchantCreateDTOMapper = new MerchantCreateDTOMapper();

  @BeforeEach
  void setUp() {

    merchantService = new MerchantServiceImpl(
        merchantDetailServiceMock,
        merchantListServiceMock,
        merchantProcessOperationService,
        merchantUpdatingInitiativeService,
        merchantUpdateIbanService,
        merchantRepositoryMock,
        uploadingMerchantServiceMock,
        initiative2InitiativeDTOMapper,
        merchantCreateDTOMapper,
        pointOfSaleRepositoryMock,
        merchantValidatorMock,
        keycloakAdminClientMock,
        REALM,
        pdndConnectorMock,
        initiativeRestClientMock);
  }

  @AfterEach
  void verifyNoMoreMockInteractions() {
    verifyNoMoreInteractions(
        merchantDetailServiceMock,
        merchantListServiceMock,
        merchantRepositoryMock,
        merchantProcessOperationService,
        merchantUpdatingInitiativeService,
        uploadingMerchantServiceMock,
        merchantUpdateIbanService,
        pointOfSaleRepositoryMock,
        merchantValidatorMock,
        keycloakAdminClientMock);
  }

  @Test
  void uploadMerchantFile() {
    MerchantUpdateDTO merchantUpdateDTO = MerchantUpdateDTOFaker.mockInstance(1);
    MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
        "Content".getBytes());
    when(uploadingMerchantServiceMock.uploadMerchantFile(any(), anyString(),
        anyString(), anyString(), anyString())).thenReturn(
        merchantUpdateDTO);

    MerchantUpdateDTO result = merchantService.uploadMerchantFile(file, ORGANIZATION_ID,
        INITIATIVE_ID, "ORGANIZATION_USER_ID", ACQUIRER_ID);
    Assertions.assertNotNull(result);
  }

  @Test
  void getMerchantDetail1() {
    MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantDetailServiceMock.getMerchantDetail(anyString())).thenReturn(dto);

    MerchantDetailDTO result = merchantService.getMerchantDetail(MERCHANT_ID);
    assertNotNull(result);
  }


  @Test
  void getMerchantDetail() {
    MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantDetailServiceMock.getMerchantDetail(anyString(), anyString(),
        anyString())).thenReturn(dto);

    MerchantDetailDTO result = merchantService.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID,
        MERCHANT_ID);
    assertNotNull(result);
  }

  @Test
  void getMerchantDetailByMerchantIdAndInitiativeId() {
    MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantDetailServiceMock.getMerchantDetail(anyString(),
        anyString())).thenReturn(merchantDetailDTO);

    MerchantDetailDTO result = merchantService.getMerchantDetail(MERCHANT_ID, INITIATIVE_ID);
    assertNotNull(result);
  }

  @Test
  void getMerchantList() {
    MerchantListDTO dto = new MerchantListDTO();
    when(merchantListServiceMock.getMerchantList(anyString(), anyString(),
        anyString(), any())).thenReturn(dto);

    MerchantListDTO result = merchantService.getMerchantList(ORGANIZATION_ID, INITIATIVE_ID,
        MERCHANT_ID, null);
    assertNotNull(result);
  }

  @Test
  void getMerchantListByInitiativeId() {
    MerchantListDTO dto = new MerchantListDTO();
    when(merchantListServiceMock.getMerchantList(anyString(), any())).thenReturn(dto);

    MerchantListDTO result = merchantService.getMerchantList(INITIATIVE_ID, null);
    assertNotNull(result);
  }

  @Test
  void retrieveMerchantId() {
    Merchant merchant = MerchantFaker.mockInstance(1);

    when(merchantRepositoryMock.retrieveByAcquirerIdAndFiscalCode(anyString(),
        anyString())).thenReturn(Optional.of(merchant));

    String merchantIdOkResult = merchantService.retrieveMerchantId(merchant.getAcquirerId(),
        merchant.getFiscalCode());

    assertNotNull(merchantIdOkResult);
    Assertions.assertEquals(merchant.getMerchantId(), merchantIdOkResult);
  }

  @Test
  void retrieveMerchantId_NotFound() {

    doReturn(Optional.empty()).when(merchantRepositoryMock)
        .retrieveByAcquirerIdAndFiscalCode(any(), eq("DUMMYFISCALCODE"));

    String merchantIdNotFoundResult = merchantService.retrieveMerchantId("DUMMYACQUIRERID",
        "DUMMYFISCALCODE");

    assertNull(merchantIdNotFoundResult);
    verify(merchantRepositoryMock).retrieveByAcquirerIdAndFiscalCode(anyString(),
        anyString());
  }

  @Test
  void getMerchantInitiativeList() {
    Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
        .initiativeList(List.of(
            InitiativeFaker.mockInstance(1),
            InitiativeFaker.mockInstance(2)))
        .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    List<it.gov.pagopa.merchant.dto.InitiativeDTO> result = merchantService.getMerchantInitiativeList(MERCHANT_ID);

    assertEquals(
        merchant.getInitiativeList().stream()
            .map(initiative2InitiativeDTOMapper::apply)
            .toList(),
        result);
  }

  @Test
  void getMerchantInitiativeList_sortedAlphabeticallyByInitiativeName() {
    Initiative initiativeC = Initiative.builder()
        .initiativeId("ID_C").initiativeName("Charlie")
        .status(MerchantConstants.INITIATIVE_PUBLISHED).build();
    Initiative initiativeA = Initiative.builder()
        .initiativeId("ID_A").initiativeName("Alpha")
        .status(MerchantConstants.INITIATIVE_PUBLISHED).build();
    Initiative initiativeB = Initiative.builder()
        .initiativeId("ID_B").initiativeName("Bravo")
        .status(MerchantConstants.INITIATIVE_PUBLISHED).build();

    Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
        .initiativeList(List.of(initiativeC, initiativeA, initiativeB))
        .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    List<it.gov.pagopa.merchant.dto.InitiativeDTO> result = merchantService.getMerchantInitiativeList(MERCHANT_ID);

    assertEquals(3, result.size());
    assertEquals("Alpha", result.get(0).getInitiativeName());
    assertEquals("Bravo", result.get(1).getInitiativeName());
    assertEquals("Charlie", result.get(2).getInitiativeName());
  }

  @Test
  void getMerchantInitiativeList_emptyList() {

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.empty());

    List<it.gov.pagopa.merchant.dto.InitiativeDTO> result = merchantService.getMerchantInitiativeList(MERCHANT_ID);

    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void processOperation() {
    QueueCommandOperationDTO queueCommandOperationDTO = QueueCommandOperationDTO.builder()
        .entityId(INITIATIVE_ID)
        .operationType(OPERATION_TYPE_DELETE_INITIATIVE)
        .build();

    merchantService.processOperation(queueCommandOperationDTO);

    verify(merchantProcessOperationService).processOperation(queueCommandOperationDTO);
  }

  @Test
  void updatingMerchantInitiative() {
    QueueInitiativeDTO queueInitiativeDTO = QueueInitiativeDTO.builder()
        .initiativeId(INITIATIVE_ID)
        .initiativeRewardType("DISCOUNT")
        .build();

    merchantService.updatingInitiative(queueInitiativeDTO);
    verify(merchantUpdatingInitiativeService, times(1)).updatingInitiative(
        queueInitiativeDTO);
  }

  @Test
  void updateIban_delegatesCallAndReturnsResult() {
    // Given
    MerchantIbanPatchDTO merchantIbanPatchDTO = new MerchantIbanPatchDTO(
        "test@mail.com","IT60X0542811101000000123456", "New Holder");
    MerchantDetailDTO expectedDto = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantUpdateIbanService.patchMerchant(MERCHANT_ID, INITIATIVE_ID,
        merchantIbanPatchDTO))
        .thenReturn(expectedDto);

    // When
    MerchantDetailDTO result = merchantService.patchMerchant(MERCHANT_ID,
        INITIATIVE_ID,
        merchantIbanPatchDTO);

    // Then
    assertNotNull(result);
    assertEquals(expectedDto, result);

    // Verify that the call was delegated to the correct service
    verify(merchantUpdateIbanService).patchMerchant(MERCHANT_ID, INITIATIVE_ID,
        merchantIbanPatchDTO);
  }

  @Test
  void updateIban_whenServiceThrowsIllegalArgumentException_propagatesException() {
    // Given
    MerchantIbanPatchDTO merchantIbanPatchDTO = new MerchantIbanPatchDTO("TEST","INVALID_IBAN", null);

    // Mock the underlying service to throw an exception
    when(merchantUpdateIbanService.patchMerchant(MERCHANT_ID, INITIATIVE_ID,
        merchantIbanPatchDTO))
        .thenThrow(new IllegalArgumentException("Invalid IBAN format."));

    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> merchantService.patchMerchant(MERCHANT_ID, INITIATIVE_ID,
            merchantIbanPatchDTO));

    assertEquals("Invalid IBAN format.", exception.getMessage());

    // Verify the call was still made
    verify(merchantUpdateIbanService).patchMerchant(MERCHANT_ID, INITIATIVE_ID,
        merchantIbanPatchDTO);
  }

  @Test
  void updateIban_whenServiceThrowsMerchantNotFoundException_propagatesException() {
    // Given
    MerchantIbanPatchDTO merchantIbanPatchDTO = new MerchantIbanPatchDTO("test@mail.com",
        "IT60X0542811101000000123456", null);
    String expectedExceptionMessage = String.format("Merchant with id %s not found.", MERCHANT_ID);

    // Mock the underlying service to throw an exception
    when(merchantUpdateIbanService.patchMerchant(MERCHANT_ID, INITIATIVE_ID,
        merchantIbanPatchDTO))
        .thenThrow(new MerchantNotFoundException(expectedExceptionMessage));

    // When & Then
    MerchantNotFoundException exception = assertThrows(MerchantNotFoundException.class,
        () -> merchantService.patchMerchant(MERCHANT_ID, INITIATIVE_ID,
            merchantIbanPatchDTO));

    assertEquals(expectedExceptionMessage, exception.getMessage());

    // Verify the call was still made
    verify(merchantUpdateIbanService).patchMerchant(MERCHANT_ID, INITIATIVE_ID,
        merchantIbanPatchDTO);
  }

  @Test
  void retrieveOrCreateMerchantIfNotExists_TestKO() {
    String acquirerId = "ACQ123";
    String businessName = "Test Business";
    String fiscalCode = "ABCDEF12G34H567I";
    String iban = "IT60X0542811101000000123456";
    String ibanHolder = "Test Iban Holder";

    MerchantCreateDTO dto = MerchantCreateDTO.builder()
        .businessName(businessName)
        .fiscalCode(fiscalCode)
        .acquirerId(acquirerId)
        .iban(iban)
        .ibanHolder(ibanHolder)
        .build();

    MongoException mongoException = mock(MongoException.class);
    when(merchantRepositoryMock.findByFiscalCode(fiscalCode))
        .thenThrow(mongoException);

    assertThrows(MongoException.class,
        () -> merchantService.retrieveOrCreateMerchantIfNotExists(dto));

    verify(merchantRepositoryMock).findByFiscalCode(fiscalCode);
    verify(merchantRepositoryMock, never()).save(any(Merchant.class));
  }

  @Test
  void createOrRetrieveMerchantIfNotExists_success_withSpy() {
    String acquirerId = "ACQ123";
    String businessName = "Test Business";
    String fiscalCode = "ABCDEF12G34H567I";
    String iban = "IT60X0542811101000000123456";
    String ibanHolder = "Test Iban Holder";
    String expectedMerchantId = Utilities.toUUID(fiscalCode + "_" + acquirerId);
    MerchantCreateDTO dto = MerchantCreateDTO.builder()
        .businessName(businessName)
        .fiscalCode(fiscalCode)
        .acquirerId(acquirerId)
        .iban(iban)
        .ibanHolder(ibanHolder)
        .build();

    when(merchantRepositoryMock.findByFiscalCode(fiscalCode))
        .thenReturn(Optional.empty());

    MerchantServiceImpl spyService = spy(merchantService);

    String result = spyService.retrieveOrCreateMerchantIfNotExists(dto);

    assertEquals(expectedMerchantId, result);
    verify(merchantRepositoryMock).save(any(Merchant.class));
  }

  @Test
  void retrieveOrCreateMerchantIfNotExists_AlreadyExists() {
    String acquirerId = "ACQ123";
    String businessName = "Test Business";
    String fiscalCode = "ABCDEF12G34H567I";
    String iban = "IT60X0542811101000000123456";
    String ibanHolder = "Test Iban Holder";
    String expectedMerchantId = Utilities.toUUID(fiscalCode + "_" + acquirerId);
    MerchantCreateDTO dto = MerchantCreateDTO.builder()
        .businessName(businessName)
        .fiscalCode(fiscalCode)
        .acquirerId(acquirerId)
        .iban(iban)
        .ibanHolder(ibanHolder)
        .build();

    Merchant merchant = Merchant.builder()
        .fiscalCode(fiscalCode)
        .merchantId(expectedMerchantId)
        .businessName("businessName")
        .build();

    when(merchantRepositoryMock.findByFiscalCode(fiscalCode))
        .thenReturn(Optional.of(merchant));

    when(merchantRepositoryMock.save(merchant))
        .thenReturn(merchant);
    String merchantIDUpdated = merchantService.retrieveOrCreateMerchantIfNotExists(dto);

    assertEquals(expectedMerchantId, merchantIDUpdated);
    verify(merchantRepositoryMock).findByFiscalCode(fiscalCode);
    verify(merchantRepositoryMock).save(any(Merchant.class));
  }

  @Test
  void updateMerchant_updatesFieldsCorrectly() {
    // Given
    String existingMerchantId = "EXISTING_MERCHANT_ID";
    LocalDateTime existingActivationDate = LocalDateTime.now().minusDays(1);
    LocalDateTime newActivationDate = LocalDateTime.now();
    Merchant existingMerchant = Merchant.builder()
        .merchantId(existingMerchantId)
        .iban("OLD_IBAN")
        .businessName("Old Business Name")
        .ibanHolder("Old Iban Holder")
        .activationDate(existingActivationDate)
        .build();

    MerchantCreateDTO updateDTO = MerchantCreateDTO.builder()
        .iban("NEW_IBAN")
        .businessName("New Business Name")
        .ibanHolder("New Iban Holder")
        .activationDate(newActivationDate)
        .build();

    // Mock the repository to return the existing merchant
    when(merchantRepositoryMock.findByFiscalCode(updateDTO.getFiscalCode()))
        .thenReturn(Optional.of(existingMerchant));

    // When
    merchantService.retrieveOrCreateMerchantIfNotExists(updateDTO);

    // Then
    assertEquals("NEW_IBAN", existingMerchant.getIban());
    assertEquals("New Business Name", existingMerchant.getBusinessName());
    assertEquals("New Iban Holder", existingMerchant.getIbanHolder());
    assertEquals(newActivationDate, existingMerchant.getActivationDate());
    verify(merchantRepositoryMock).save(existingMerchant);
  }

  @Test
  void updateMerchant_doesNotUpdateWhenFieldsAreBlank() {
    // Given
    String existingMerchantId = "EXISTING_MERCHANT_ID";
    LocalDateTime activationDate = LocalDateTime.now();
    Merchant existingMerchant = Merchant.builder()
        .merchantId(existingMerchantId)
        .iban("OLD_IBAN")
        .businessName("Old Business Name")
        .ibanHolder("Old Iban Holder")
        .activationDate(activationDate)
        .build();

    MerchantCreateDTO updateDTO = MerchantCreateDTO.builder()
        .iban("") // Blank
        .businessName(null) // Null
        .ibanHolder("") // Blank
        .build();

    // Mock the repository to return the existing merchant
    when(merchantRepositoryMock.findByFiscalCode(updateDTO.getFiscalCode()))
        .thenReturn(Optional.of(existingMerchant));

    // When
    merchantService.retrieveOrCreateMerchantIfNotExists(updateDTO);

    // Then
    assertEquals("OLD_IBAN", existingMerchant.getIban());
    assertEquals("Old Business Name", existingMerchant.getBusinessName());
    assertEquals("Old Iban Holder", existingMerchant.getIbanHolder());
    assertEquals(activationDate, existingMerchant.getActivationDate());
    verify(merchantRepositoryMock).save(existingMerchant);
  }

  @Test
  void updateMerchant_updatesOnlyProvidedFields() {
    // Given
    String existingMerchantId = "EXISTING_MERCHANT_ID";
    LocalDateTime activationDate = LocalDateTime.now();
    LocalDateTime activatioDateTimeNew =LocalDateTime.now().plusDays(2);
    Merchant existingMerchant = Merchant.builder()
        .merchantId(existingMerchantId)
        .iban("OLD_IBAN")
        .businessName("Old Business Name")
        .ibanHolder("Old Iban Holder")
        .activationDate(activationDate)
        .build();

    MerchantCreateDTO updateDTO = MerchantCreateDTO.builder()
        .iban("NEW_IBAN") // Only updating IBAN
        .activationDate(activatioDateTimeNew)
        .build();

    // Mock the repository to return the existing merchant
    when(merchantRepositoryMock.findByFiscalCode(updateDTO.getFiscalCode()))
        .thenReturn(Optional.of(existingMerchant));

    // When
    merchantService.retrieveOrCreateMerchantIfNotExists(updateDTO);

    // Then
    assertEquals("NEW_IBAN", existingMerchant.getIban());
    assertEquals("Old Business Name", existingMerchant.getBusinessName());
    assertEquals("Old Iban Holder", existingMerchant.getIbanHolder());
    assertEquals(activatioDateTimeNew, existingMerchant.getActivationDate());
    verify(merchantRepositoryMock).save(existingMerchant);
  }

  @Test
  void deactivateMerchant_dryRun_shouldReturnMessageWithoutDisablingMerchant() {
    Merchant merchant = Merchant.builder()
        .merchantId(MERCHANT_ID)
        .enabled(true)
        .build();
    List<PointOfSale> posList = List.of();

    when(merchantRepositoryMock.retrieveByMerchantIdAndInitiativeId(MERCHANT_ID, INITIATIVE_ID))
        .thenReturn(Optional.of(merchant));
    when(pointOfSaleRepositoryMock.findByMerchantId(MERCHANT_ID))
        .thenReturn(posList);

    MerchantWithdrawalResponse response = merchantService.deactivateMerchant(MERCHANT_ID, INITIATIVE_ID, true);

    assertNotNull(response);
    assertTrue(response.getMessage().contains("can be safely deactivated"));
    verify(merchantValidatorMock).validateMerchantWithdrawal(merchant, INITIATIVE_ID);
    verify(pointOfSaleRepositoryMock, never()).deleteByMerchantId(any());
    verify(merchantRepositoryMock, never()).save(any());
  }

  @Test
  void deactivateMerchant_actualRun_shouldDisableMerchantAndDeletePointsOfSale() {
    Merchant merchant = Merchant.builder()
        .merchantId(MERCHANT_ID)
        .enabled(true)
        .build();
    List<PointOfSale> posList = List.of(
        PointOfSale.builder().id("POS1").contactEmail("user1@test.com").build(),
        PointOfSale.builder().id("POS2").contactEmail("user2@test.com").build()
    );

    when(merchantRepositoryMock.retrieveByMerchantIdAndInitiativeId(MERCHANT_ID, INITIATIVE_ID))
        .thenReturn(Optional.of(merchant));
    when(pointOfSaleRepositoryMock.findByMerchantId(MERCHANT_ID))
        .thenReturn(posList);

    RealmResource realmResourceMock = mock(RealmResource.class);
    UsersResource usersResourceMock = mock(UsersResource.class);
    when(keycloakAdminClientMock.realm(REALM)).thenReturn(realmResourceMock);
    when(realmResourceMock.users()).thenReturn(usersResourceMock);

    UserRepresentation user1 = new UserRepresentation();
    user1.setId("user1Id");
    UserRepresentation user2 = new UserRepresentation();
    user2.setId("user2Id");

    when(usersResourceMock.searchByEmail("user1@test.com", true)).thenReturn(List.of(user1));
    when(usersResourceMock.searchByEmail("user2@test.com", true)).thenReturn(List.of(user2));

    UserResource userResource1 = mock(UserResource.class);
    UserResource userResource2 = mock(UserResource.class);
    when(usersResourceMock.get("user1Id")).thenReturn(userResource1);
    when(usersResourceMock.get("user2Id")).thenReturn(userResource2);
    doNothing().when(userResource1).logout();
    doNothing().when(userResource1).remove();
    doNothing().when(userResource2).logout();
    doNothing().when(userResource2).remove();

    MerchantWithdrawalResponse response = merchantService.deactivateMerchant(MERCHANT_ID, INITIATIVE_ID, false);

    assertNotNull(response);
    assertTrue(response.getMessage().contains("has been deactivated"));
    assertFalse(merchant.isEnabled());

    verify(merchantRepositoryMock).retrieveByMerchantIdAndInitiativeId(MERCHANT_ID, INITIATIVE_ID);
    verify(merchantRepositoryMock).save(merchant);
    verify(pointOfSaleRepositoryMock).findByMerchantId(MERCHANT_ID);
    verify(pointOfSaleRepositoryMock).deleteByMerchantId(MERCHANT_ID);
    verify(merchantValidatorMock).validateMerchantWithdrawal(merchant, INITIATIVE_ID);
    verify(usersResourceMock).searchByEmail("user1@test.com", true);
    verify(usersResourceMock).searchByEmail("user2@test.com", true);
    verify(userResource1).logout();
    verify(userResource1).remove();
    verify(userResource2).logout();
    verify(userResource2).remove();
  }

  @Test
  void deactivateMerchant_merchantNotFound_shouldThrowException() {
    when(merchantRepositoryMock.retrieveByMerchantIdAndInitiativeId(MERCHANT_ID, INITIATIVE_ID))
        .thenReturn(Optional.empty());

    MerchantNotFoundException exception = assertThrows(MerchantNotFoundException.class,
        () -> merchantService.deactivateMerchant(MERCHANT_ID, INITIATIVE_ID, true));

    assertTrue(exception.getMessage().contains(MERCHANT_ID));
  }

  @Test
  void getMerchantByMerchantId_found() {
    Merchant merchant = Merchant.builder()
        .merchantId(MERCHANT_ID)
        .businessName("Test Business")
        .fiscalCode("12345678901")
        .vatNumber("IT12345678901")
        .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    Merchant result = merchantService.getMerchantByMerchantId(MERCHANT_ID);

    assertNotNull(result);
    assertEquals(MERCHANT_ID, result.getMerchantId());
    assertEquals("Test Business", result.getBusinessName());

    verify(merchantRepositoryMock).findById(MERCHANT_ID);
  }

  @Test
  void getMerchantByMerchantId_notFound_throwsException() {
    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.empty());

    MerchantNotFoundException exception = Assertions.assertThrows(
        MerchantNotFoundException.class,
        () -> merchantService.getMerchantByMerchantId(MERCHANT_ID)
    );

    assertEquals(String.format("Merchant with id %s not found", MERCHANT_ID), exception.getMessage());

    verify(merchantRepositoryMock).findById(MERCHANT_ID);
  }

  @Test
  void verifyMerchantExists_merchantFound_doesNotThrow() {
    MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantDetailServiceMock.getMerchantDetail(MERCHANT_ID)).thenReturn(dto);

    assertDoesNotThrow(() -> merchantService.verifyMerchantExists(MERCHANT_ID));

    verify(merchantDetailServiceMock).getMerchantDetail(MERCHANT_ID);
  }

  @Test
  void verifyMerchantExists_merchantNotFound_throwsMerchantNotFoundException() {
    when(merchantDetailServiceMock.getMerchantDetail(MERCHANT_ID)).thenReturn(null);

    MerchantNotFoundException exception = assertThrows(MerchantNotFoundException.class,
            () -> merchantService.verifyMerchantExists(MERCHANT_ID));

    assertTrue(exception.getMessage().contains(MERCHANT_ID));
    verify(merchantDetailServiceMock).getMerchantDetail(MERCHANT_ID);
  }

  @Test
  void processMerchantInitiatives_success() {
    String merchantId = "merchant123";
    String initiativeName = "Initiative 1";
    Pageable pageable = Pageable.ofSize(10);

    Merchant merchant = new Merchant();
    merchant.setMerchantId(merchantId);
    merchant.setVatNumber("123456789");
    merchant.setAtecoCodes(List.of("1234"));
    merchant.setInitiativeList(List.of(Initiative.builder().initiativeId("initiative1").build()));

    List<String> newAtecoCodes = List.of("1234", "5678");
    PageResponse<InitiativeResponse> mockResponse = new PageResponse<>(
            List.of(InitiativeResponse.builder().initiativeId("initiative2").status("ACTIVE").build()),
            0, 10, 1
    );

    ResponseEntity<PageResponse<InitiativeResponse>> response = ResponseEntity.of(Optional.of(mockResponse));
    when(merchantRepositoryMock.findById(merchantId)).thenReturn(Optional.of(merchant));
    when(pdndConnectorMock.retrieveAtecoCodes(merchant.getVatNumber())).thenReturn(newAtecoCodes);
    when(initiativeRestClientMock.searchInitiatives(any(), eq(pageable))).thenReturn(response);

    Page<InitiativeResponse> result = merchantService.processMerchantInitiatives(merchantId, initiativeName, pageable);


    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("initiative2", result.getContent().getFirst().getInitiativeId());
    verify(merchantRepositoryMock).save(merchant);
    verify(initiativeRestClientMock).searchInitiatives(any(), eq(pageable));
  }

  @Test
  void processMerchantInitiatives_nullMerchantAtecoCodes_success() {
    String merchantId = "merchant123";
    String initiativeName = "Initiative 1";
    Pageable pageable = Pageable.ofSize(10);

    Merchant merchant = new Merchant();
    merchant.setMerchantId(merchantId);
    merchant.setVatNumber("123456789");
    merchant.setAtecoCodes(null);
    merchant.setInitiativeList(List.of(Initiative.builder().initiativeId("initiative1").build()));

    List<String> newAtecoCodes = List.of("1234", "5678");
    PageResponse<InitiativeResponse> mockResponse = new PageResponse<>(
            List.of(InitiativeResponse.builder().initiativeId("initiative2").status("ACTIVE").build()),
            0, 10, 1
    );

    ResponseEntity<PageResponse<InitiativeResponse>> response = ResponseEntity.of(Optional.of(mockResponse));
    when(merchantRepositoryMock.findById(merchantId)).thenReturn(Optional.of(merchant));
    when(pdndConnectorMock.retrieveAtecoCodes(merchant.getVatNumber())).thenReturn(newAtecoCodes);
    when(initiativeRestClientMock.searchInitiatives(any(), eq(pageable))).thenReturn(response);

    Page<InitiativeResponse> result = merchantService.processMerchantInitiatives(merchantId, initiativeName, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("initiative2", result.getContent().getFirst().getInitiativeId());
    verify(merchantRepositoryMock).save(merchant);
    verify(initiativeRestClientMock).searchInitiatives(any(), eq(pageable));
  }

  @Test
  void processMerchantInitiatives_nullPdndAtecoCodes_success() {
    String merchantId = "merchant123";
    String initiativeName = "Initiative 1";
    Pageable pageable = Pageable.ofSize(10);

    Merchant merchant = new Merchant();
    merchant.setMerchantId(merchantId);
    merchant.setVatNumber("123456789");
    merchant.setAtecoCodes(null);
    merchant.setInitiativeList(List.of(Initiative.builder().initiativeId("initiative1").build()));

    PageResponse<InitiativeResponse> mockResponse = new PageResponse<>(
            List.of(InitiativeResponse.builder().initiativeId("initiative2").status("ACTIVE").build()),
            0, 10, 1
    );

    ResponseEntity<PageResponse<InitiativeResponse>> response = ResponseEntity.of(Optional.of(mockResponse));
    when(merchantRepositoryMock.findById(merchantId)).thenReturn(Optional.of(merchant));
    when(pdndConnectorMock.retrieveAtecoCodes(merchant.getVatNumber())).thenReturn(null);
    when(initiativeRestClientMock.searchInitiatives(any(), eq(pageable))).thenReturn(response);

    Page<InitiativeResponse> result = merchantService.processMerchantInitiatives(merchantId, initiativeName, pageable);

    ArgumentCaptor<InitiativeSearchRequest> requestCaptor = ArgumentCaptor.forClass(InitiativeSearchRequest.class);
    verify(initiativeRestClientMock).searchInitiatives(requestCaptor.capture(), eq(pageable));

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("initiative2", result.getContent().getFirst().getInitiativeId());
    assertNotNull(requestCaptor.getValue());
    assertEquals(Collections.emptyList(), requestCaptor.getValue().getAtecoCodes());
    verify(merchantRepositoryMock, never()).save(merchant);
  }

  @Test
  void processMerchantInitiatives_merchantNotFound() {
    String merchantId = "merchant123";
    String initiativeName = "Initiative 1";
    Pageable pageable = Pageable.ofSize(10);

    when(merchantRepositoryMock.findById(merchantId)).thenReturn(Optional.empty());

    assertThrows(MerchantNotFoundException.class,
            () -> merchantService.processMerchantInitiatives(merchantId, initiativeName, pageable));
  }

}