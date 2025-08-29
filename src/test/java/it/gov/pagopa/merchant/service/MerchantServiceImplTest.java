package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.*;
import it.gov.pagopa.merchant.dto.initiative.AdditionalInfoDTO;
import it.gov.pagopa.merchant.dto.initiative.GeneralInfoDTO;
import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import it.gov.pagopa.merchant.exception.custom.InitiativeInvocationException;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.service.merchant.*;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantUpdateDTOFaker;
import it.gov.pagopa.merchant.utils.Utilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    private MerchantServiceImpl merchantService;

    private MerchantServiceImpl merchantServiceSpy;

    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
    private static final String ACQUIRER_ID = "PAGOPA";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String OPERATION_TYPE_DELETE_INITIATIVE = "DELETE_INITIATIVE";
    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper = new Initiative2InitiativeDTOMapper();

    @BeforeEach
    void setUp() {
        String defaultInitiativesMock = "INIT1,INIT2";

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
                initiativeRestConnector
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
                merchantUpdateIbanService);
    }

    @Test
    void uploadMerchantFile() {
        MerchantUpdateDTO merchantUpdateDTO = MerchantUpdateDTOFaker.mockInstance(1);
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "Content".getBytes());
        when(uploadingMerchantServiceMock.uploadMerchantFile(any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(merchantUpdateDTO);

        MerchantUpdateDTO result = merchantService.uploadMerchantFile(file, ORGANIZATION_ID, INITIATIVE_ID, "ORGANIZATION_USER_ID", ACQUIRER_ID);
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
        when(merchantDetailServiceMock.getMerchantDetail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(dto);

        MerchantDetailDTO result = merchantService.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID);
        assertNotNull(result);
    }

    @Test
    void getMerchantDetailByMerchantIdAndInitiativeId() {
        MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);
        when(merchantDetailServiceMock.getMerchantDetail(Mockito.anyString(), Mockito.anyString())).thenReturn(merchantDetailDTO);

        MerchantDetailDTO result = merchantService.getMerchantDetail(MERCHANT_ID, INITIATIVE_ID);
        assertNotNull(result);
    }

    @Test
    void getMerchantList() {
        MerchantListDTO dto = new MerchantListDTO();
        when(merchantListServiceMock.getMerchantList(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), any())).thenReturn(dto);

        MerchantListDTO result = merchantService.getMerchantList(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID, null);
        assertNotNull(result);
    }

    @Test
    void retrieveMerchantId() {
        Merchant merchant = MerchantFaker.mockInstance(1);

        when(merchantRepositoryMock.retrieveByAcquirerIdAndFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(merchant));

        String merchantIdOkResult = merchantService.retrieveMerchantId(merchant.getAcquirerId(), merchant.getFiscalCode());

        assertNotNull(merchantIdOkResult);
        Assertions.assertEquals(merchant.getMerchantId(), merchantIdOkResult);
    }

    @Test
    void retrieveMerchantId_NotFound() {

        doReturn(Optional.empty()).when(merchantRepositoryMock)
                .retrieveByAcquirerIdAndFiscalCode(any(), Mockito.eq("DUMMYFISCALCODE"));

        String merchantIdNotFoundResult = merchantService.retrieveMerchantId("DUMMYACQUIRERID", "DUMMYFISCALCODE");

        assertNull(merchantIdNotFoundResult);
        verify(merchantRepositoryMock).retrieveByAcquirerIdAndFiscalCode(Mockito.anyString(), Mockito.anyString());
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
        verify(merchantUpdatingInitiativeService, Mockito.times(1)).updatingInitiative(queueInitiativeDTO);
    }

    @Test
    void updateIban_delegatesCallAndReturnsResult() {
        // Given
        MerchantIbanPatchDTO merchantIbanPatchDTO = new MerchantIbanPatchDTO("IT60X0542811101000000123456", "New Holder");
        MerchantDetailDTO expectedDto = MerchantDetailDTOFaker.mockInstance(1);

        when(merchantUpdateIbanService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, merchantIbanPatchDTO))
                .thenReturn(expectedDto);

        // When
        MerchantDetailDTO result = merchantService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
                merchantIbanPatchDTO);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto, result);

        // Verify that the call was delegated to the correct service
        verify(merchantUpdateIbanService).updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, merchantIbanPatchDTO);
    }

    @Test
    void updateIban_whenServiceThrowsIllegalArgumentException_propagatesException() {
        // Given
        MerchantIbanPatchDTO merchantIbanPatchDTO = new MerchantIbanPatchDTO("INVALID_IBAN", null);

        // Mock the underlying service to throw an exception
        when(merchantUpdateIbanService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, merchantIbanPatchDTO))
                .thenThrow(new IllegalArgumentException("Invalid IBAN format."));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> merchantService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
                        merchantIbanPatchDTO));

        assertEquals("Invalid IBAN format.", exception.getMessage());

        // Verify the call was still made
        verify(merchantUpdateIbanService).updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, merchantIbanPatchDTO);
    }

    @Test
    void updateIban_whenServiceThrowsMerchantNotFoundException_propagatesException() {
        // Given
        MerchantIbanPatchDTO merchantIbanPatchDTO = new MerchantIbanPatchDTO("IT60X0542811101000000123456", null);
        String expectedExceptionMessage = String.format("Merchant with id %s not found.", MERCHANT_ID);

        // Mock the underlying service to throw an exception
        when(merchantUpdateIbanService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, merchantIbanPatchDTO))
                .thenThrow(new MerchantNotFoundException(expectedExceptionMessage));

        // When & Then
        MerchantNotFoundException exception = assertThrows(MerchantNotFoundException.class,
                () -> merchantService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID,
                        merchantIbanPatchDTO));

        assertEquals(expectedExceptionMessage, exception.getMessage());

        // Verify the call was still made
        verify(merchantUpdateIbanService).updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, merchantIbanPatchDTO);
    }

    @Test
    void createMerchantIfNotExists_TestKO() {
        String acquirerId = "ACQ123";
        String businessName = "Test Business";
        String fiscalCode = "ABCDEF12G34H567I";

        Merchant existing = Merchant.builder()
                .merchantId("MERCHANT123")
                .fiscalCode(fiscalCode)
                .build();

        Mockito.when(merchantRepositoryMock.findByFiscalCode(fiscalCode))
                .thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> merchantService.createMerchantIfNotExists(acquirerId, businessName, fiscalCode));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("already exists"));

        Mockito.verify(merchantRepositoryMock).findByFiscalCode(fiscalCode);
        Mockito.verify(merchantRepositoryMock, Mockito.never()).save(Mockito.any(Merchant.class));
    }

    @Test
    void getInitiativeInfo_connectorThrowsException() throws Exception {
        when(initiativeRestConnector.getInitiativeBeneficiaryView("INIT1"))
                .thenThrow(new RuntimeException("REST error"));

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
    void createMerchantIfNotExists_success_withSpy() {
        String acquirerId = "ACQ123";
        String businessName = "Test Business";
        String fiscalCode = "ABCDEF12G34H567I";
        String expectedMerchantId = Utilities.toUUID(fiscalCode + "_" + acquirerId);

        when(merchantRepositoryMock.findByFiscalCode(fiscalCode))
                .thenReturn(Optional.empty());

        MerchantServiceImpl spyService = Mockito.spy(merchantService);

        when(initiativeRestConnector.getInitiativeBeneficiaryView(anyString()))
                .thenAnswer(invocation -> {
                    String initiativeId = invocation.getArgument(0);

                    InitiativeBeneficiaryViewDTO dto = new InitiativeBeneficiaryViewDTO();
                    dto.setInitiativeId(initiativeId);
                    dto.setInitiativeName("Test Initiative");
                    dto.setOrganizationId("ORG1");
                    dto.setOrganizationName("Organization 1");

                    AdditionalInfoDTO additionalInfo = new AdditionalInfoDTO();
                    additionalInfo.setServiceId("SERVICE1");
                    dto.setAdditionalInfo(additionalInfo);

                    GeneralInfoDTO general = new GeneralInfoDTO();
                    general.setStartDate(LocalDate.now().minusDays(1));
                    general.setEndDate(LocalDate.now().plusDays(1));
                    dto.setGeneral(general);

                    dto.setStatus("ACTIVE");
                    return dto;
                });

        String result = spyService.createMerchantIfNotExists(acquirerId, businessName, fiscalCode);

        assertEquals(expectedMerchantId, result);
        verify(merchantRepositoryMock).save(any(Merchant.class));
        verify(initiativeRestConnector, times(2)).getInitiativeBeneficiaryView(anyString());
    }

}