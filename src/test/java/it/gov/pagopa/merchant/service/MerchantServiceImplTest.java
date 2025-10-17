package it.gov.pagopa.merchant.service;

import com.mongodb.MongoException;
import feign.FeignException;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.*;
import it.gov.pagopa.merchant.dto.initiative.AdditionalInfoDTO;
import it.gov.pagopa.merchant.dto.initiative.GeneralInfoDTO;
import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import it.gov.pagopa.merchant.exception.custom.InitiativeInvocationException;
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
import java.time.LocalDateTime;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
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

  private MerchantServiceImpl merchantService;

  private MerchantServiceImpl merchantServiceSpy;

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
    List<String> defaultInitiativesMock = List.of("INIT1", "INIT2");

    merchantService = new MerchantServiceImpl(
        merchantDetailServiceMock,
        merchantListServiceMock,
        merchantProcessOperationService,
        merchantUpdatingInitiativeService,
        merchantUpdateIbanService,
        merchantRepositoryMock,
        uploadingMerchantServiceMock,
        initiative2InitiativeDTOMapper,
        defaultInitiativesMock,
        initiativeRestConnector,
        merchantCreateDTOMapper,
        pointOfSaleRepositoryMock,
        merchantValidatorMock,
        keycloakAdminClientMock,
        REALM
    );
    merchantServiceSpy = Mockito.spy(merchantService);
  }

  @AfterEach
  void verifyNoMoreMockInteractions() {
    Mockito.verifyNoMoreInteractions(
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
    when(uploadingMerchantServiceMock.uploadMerchantFile(any(), Mockito.anyString(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(
        merchantUpdateDTO);

    MerchantUpdateDTO result = merchantService.uploadMerchantFile(file, ORGANIZATION_ID,
        INITIATIVE_ID, "ORGANIZATION_USER_ID", ACQUIRER_ID);
    Assertions.assertNotNull(result);
  }

  @Test
  void getMerchantDetail1() {
    MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantDetailServiceMock.getMerchantDetail(Mockito.anyString())).thenReturn(dto);

    MerchantDetailDTO result = merchantService.getMerchantDetail(MERCHANT_ID);
    assertNotNull(result);
  }


  @Test
  void getMerchantDetail() {
    MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantDetailServiceMock.getMerchantDetail(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(dto);

    MerchantDetailDTO result = merchantService.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID,
        MERCHANT_ID);
    assertNotNull(result);
  }

  @Test
  void getMerchantDetailByMerchantIdAndInitiativeId() {
    MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantDetailServiceMock.getMerchantDetail(Mockito.anyString(),
        Mockito.anyString())).thenReturn(merchantDetailDTO);

    MerchantDetailDTO result = merchantService.getMerchantDetail(MERCHANT_ID, INITIATIVE_ID);
    assertNotNull(result);
  }

  @Test
  void getMerchantList() {
    MerchantListDTO dto = new MerchantListDTO();
    when(merchantListServiceMock.getMerchantList(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), any())).thenReturn(dto);

    MerchantListDTO result = merchantService.getMerchantList(ORGANIZATION_ID, INITIATIVE_ID,
        MERCHANT_ID, null);
    assertNotNull(result);
  }

  @Test
  void retrieveMerchantId() {
    Merchant merchant = MerchantFaker.mockInstance(1);

    when(merchantRepositoryMock.retrieveByAcquirerIdAndFiscalCode(Mockito.anyString(),
        Mockito.anyString())).thenReturn(Optional.of(merchant));

    String merchantIdOkResult = merchantService.retrieveMerchantId(merchant.getAcquirerId(),
        merchant.getFiscalCode());

    assertNotNull(merchantIdOkResult);
    Assertions.assertEquals(merchant.getMerchantId(), merchantIdOkResult);
  }

  @Test
  void retrieveMerchantId_NotFound() {

    doReturn(Optional.empty()).when(merchantRepositoryMock)
        .retrieveByAcquirerIdAndFiscalCode(any(), Mockito.eq("DUMMYFISCALCODE"));

    String merchantIdNotFoundResult = merchantService.retrieveMerchantId("DUMMYACQUIRERID",
        "DUMMYFISCALCODE");

    assertNull(merchantIdNotFoundResult);
    verify(merchantRepositoryMock).retrieveByAcquirerIdAndFiscalCode(Mockito.anyString(),
        Mockito.anyString());
  }

  @Test
  void getMerchantInitiativeList() {
    Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
        .initiativeList(List.of(
            InitiativeFaker.mockInstance(1),
            InitiativeFaker.mockInstance(2)))
        .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

    List<InitiativeDTO> result = merchantService.getMerchantInitiativeList(MERCHANT_ID);

    assertEquals(
        merchant.getInitiativeList().stream()
            .map(initiative2InitiativeDTOMapper::apply)
            .toList(),
        result);
  }

  @Test
  void getMerchantInitiativeList_emptyList() {

    when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.empty());

    List<InitiativeDTO> result = merchantService.getMerchantInitiativeList(MERCHANT_ID);

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
    verify(merchantUpdatingInitiativeService, Mockito.times(1)).updatingInitiative(
        queueInitiativeDTO);
  }

  @Test
  void updateIban_delegatesCallAndReturnsResult() {
    // Given
    MerchantIbanPatchDTO merchantIbanPatchDTO = new MerchantIbanPatchDTO(
        "IT60X0542811101000000123456", "New Holder");
    MerchantDetailDTO expectedDto = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantUpdateIbanService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
        merchantIbanPatchDTO))
        .thenReturn(expectedDto);

    // When
    MerchantDetailDTO result = merchantService.updateIban(MERCHANT_ID, ORGANIZATION_ID,
        INITIATIVE_ID,
        merchantIbanPatchDTO);

    // Then
    assertNotNull(result);
    assertEquals(expectedDto, result);

    // Verify that the call was delegated to the correct service
    verify(merchantUpdateIbanService).updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
        merchantIbanPatchDTO);
  }

  @Test
  void updateIban_whenServiceThrowsIllegalArgumentException_propagatesException() {
    // Given
    MerchantIbanPatchDTO merchantIbanPatchDTO = new MerchantIbanPatchDTO("INVALID_IBAN", null);

    // Mock the underlying service to throw an exception
    when(merchantUpdateIbanService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
        merchantIbanPatchDTO))
        .thenThrow(new IllegalArgumentException("Invalid IBAN format."));

    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> merchantService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
            merchantIbanPatchDTO));

    assertEquals("Invalid IBAN format.", exception.getMessage());

    // Verify the call was still made
    verify(merchantUpdateIbanService).updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
        merchantIbanPatchDTO);
  }

  @Test
  void updateIban_whenServiceThrowsMerchantNotFoundException_propagatesException() {
    // Given
    MerchantIbanPatchDTO merchantIbanPatchDTO = new MerchantIbanPatchDTO(
        "IT60X0542811101000000123456", null);
    String expectedExceptionMessage = String.format("Merchant with id %s not found.", MERCHANT_ID);

    // Mock the underlying service to throw an exception
    when(merchantUpdateIbanService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
        merchantIbanPatchDTO))
        .thenThrow(new MerchantNotFoundException(expectedExceptionMessage));

    // When & Then
    MerchantNotFoundException exception = assertThrows(MerchantNotFoundException.class,
        () -> merchantService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
            merchantIbanPatchDTO));

    assertEquals(expectedExceptionMessage, exception.getMessage());

    // Verify the call was still made
    verify(merchantUpdateIbanService).updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
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

    MongoException mongoException = Mockito.mock(MongoException.class);
    Mockito.when(merchantRepositoryMock.findByFiscalCode(fiscalCode))
        .thenThrow(mongoException);

    assertThrows(MongoException.class,
        () -> merchantService.retrieveOrCreateMerchantIfNotExists(dto));

    Mockito.verify(merchantRepositoryMock).findByFiscalCode(fiscalCode);
    Mockito.verify(merchantRepositoryMock, Mockito.never()).save(Mockito.any(Merchant.class));
  }

  @Test
  void getInitiativeInfo_connectorThrowsException() throws Exception {
    FeignException feignException = Mockito.mock(FeignException.class);
    when(feignException.getMessage()).thenReturn("REST error");

    when(initiativeRestConnector.getInitiativeBeneficiaryView("INIT1"))
        .thenThrow(feignException);

    Method method = MerchantServiceImpl.class
        .getDeclaredMethod("getInitiativeInfo", String.class);
    method.setAccessible(true);

    InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
        () -> method.invoke(merchantServiceSpy, "INIT1"));

    Throwable cause = thrown.getCause();
    assertInstanceOf(InitiativeInvocationException.class, cause);
    assertTrue(cause.getMessage().contains(
        MerchantConstants.ExceptionMessage.INITIATIVE_CONNECTOR_ERROR));
  }

  @Test
  void getInitiativeInfo_returnsNull_throwsException() throws Exception {
    when(initiativeRestConnector.getInitiativeBeneficiaryView("INIT1"))
        .thenReturn(null);

    Method method = MerchantServiceImpl.class
        .getDeclaredMethod("getInitiativeInfo", String.class);
    method.setAccessible(true);

    InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
        () -> method.invoke(merchantServiceSpy, "INIT1"));

    Throwable cause = thrown.getCause();
    assertInstanceOf(InitiativeInvocationException.class, cause);
    assertTrue(cause.getMessage().contains("Initiative not found for id=INIT1"));
  }

  @Test
  void getInitiativeInfo_returnsValidDTO() throws Exception {
    InitiativeBeneficiaryViewDTO dtoMock = new InitiativeBeneficiaryViewDTO();
    dtoMock.setInitiativeId("INIT1");
    dtoMock.setInitiativeName("Test Initiative");

    when(initiativeRestConnector.getInitiativeBeneficiaryView("INIT1"))
        .thenReturn(dtoMock);

    Method method = MerchantServiceImpl.class
        .getDeclaredMethod("getInitiativeInfo", String.class);
    method.setAccessible(true);

    InitiativeBeneficiaryViewDTO result =
        (InitiativeBeneficiaryViewDTO) method.invoke(merchantServiceSpy, "INIT1");

    assertNotNull(result);
    assertEquals("INIT1", result.getInitiativeId());
    assertEquals("Test Initiative", result.getInitiativeName());
  }

  @Test
  void createMerchantInitiative_returnsInitiative() throws Exception {
    InitiativeBeneficiaryViewDTO dto = new InitiativeBeneficiaryViewDTO();
    dto.setInitiativeId("INIT1");
    dto.setInitiativeName("Test Initiative");
    dto.setOrganizationId("ORG1");
    dto.setOrganizationName("Organization 1");
    dto.setStatus("ACTIVE");

    AdditionalInfoDTO additionalInfo = new AdditionalInfoDTO();
    additionalInfo.setServiceId("SERVICE1");
    dto.setAdditionalInfo(additionalInfo);

    GeneralInfoDTO general = new GeneralInfoDTO();
    general.setStartDate(LocalDate.now().minusDays(1));
    general.setEndDate(LocalDate.now().plusDays(1));
    dto.setGeneral(general);

    Method method = MerchantServiceImpl.class
        .getDeclaredMethod("createMerchantInitiative", InitiativeBeneficiaryViewDTO.class);
    method.setAccessible(true);

    Initiative initiative = (Initiative) method.invoke(merchantServiceSpy, dto);

    assertNotNull(initiative);
    assertEquals("INIT1", initiative.getInitiativeId());
    assertEquals("Test Initiative", initiative.getInitiativeName());
    assertEquals("ORG1", initiative.getOrganizationId());
    assertEquals("Organization 1", initiative.getOrganizationName());
    assertEquals("SERVICE1", initiative.getServiceId());
    assertEquals(LocalDate.now().minusDays(1), initiative.getStartDate());
    assertEquals(LocalDate.now().plusDays(1), initiative.getEndDate());
    assertEquals("ACTIVE", initiative.getStatus());
    assertEquals("UPLOADED", initiative.getMerchantStatus());
    assertTrue(initiative.isEnabled());
    assertNotNull(initiative.getCreationDate());
    assertNotNull(initiative.getUpdateDate());
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

    MerchantServiceImpl spyService = Mockito.spy(merchantService);

    when(initiativeRestConnector.getInitiativeBeneficiaryView(anyString()))
        .thenAnswer(invocation -> {
          String initiativeId = invocation.getArgument(0);

          InitiativeBeneficiaryViewDTO dtoIBV = new InitiativeBeneficiaryViewDTO();
          dtoIBV.setInitiativeId(initiativeId);
          dtoIBV.setInitiativeName("Test Initiative");
          dtoIBV.setOrganizationId("ORG1");
          dtoIBV.setOrganizationName("Organization 1");

          AdditionalInfoDTO additionalInfo = new AdditionalInfoDTO();
          additionalInfo.setServiceId("SERVICE1");
          dtoIBV.setAdditionalInfo(additionalInfo);

          GeneralInfoDTO general = new GeneralInfoDTO();
          general.setStartDate(LocalDate.now().minusDays(1));
          general.setEndDate(LocalDate.now().plusDays(1));
          dtoIBV.setGeneral(general);

          dtoIBV.setStatus("ACTIVE");
          return dtoIBV;
        });

    String result = spyService.retrieveOrCreateMerchantIfNotExists(dto);

    assertEquals(expectedMerchantId, result);
    verify(merchantRepositoryMock).save(any(Merchant.class));
    verify(initiativeRestConnector, times(2)).getInitiativeBeneficiaryView(anyString());
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

    Mockito.when(merchantRepositoryMock.findByFiscalCode(fiscalCode))
        .thenReturn(Optional.of(merchant));

    Mockito.when(merchantRepositoryMock.save(merchant))
        .thenReturn(merchant);
    String merchantIDUpdated = merchantService.retrieveOrCreateMerchantIfNotExists(dto);

    assertEquals(expectedMerchantId, merchantIDUpdated);
    Mockito.verify(merchantRepositoryMock).findByFiscalCode(fiscalCode);
    Mockito.verify(merchantRepositoryMock).save(Mockito.any(Merchant.class));
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

}