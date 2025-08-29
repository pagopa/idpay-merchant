package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.merchant.dto.*;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.mapper.Initiative2InitiativeDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.service.merchant.*;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantUpdateDTOFaker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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

    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
    private static final String ACQUIRER_ID = "PAGOPA";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String OPERATION_TYPE_DELETE_INITIATIVE = "DELETE_INITIATIVE";
    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper = new Initiative2InitiativeDTOMapper();

    private MerchantServiceImpl merchantService;

    @BeforeEach
    void setUp(){
        String defaultInitiativesMock = "";

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
    void uploadMerchantFile () {
        MerchantUpdateDTO merchantUpdateDTO = MerchantUpdateDTOFaker.mockInstance(1);
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "Content".getBytes());
        when(uploadingMerchantServiceMock.uploadMerchantFile(any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(merchantUpdateDTO);

        MerchantUpdateDTO result = merchantService.uploadMerchantFile(file, ORGANIZATION_ID, INITIATIVE_ID, "ORGANIZATION_USER_ID", ACQUIRER_ID);
        Assertions.assertNotNull(result);
    }

    @Test
    void getMerchantDetail1(){
        MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
        when(merchantDetailServiceMock.getMerchantDetail(Mockito.anyString())).thenReturn(dto);

        MerchantDetailDTO result = merchantService.getMerchantDetail(MERCHANT_ID);
        assertNotNull(result);
    }


    @Test
    void getMerchantDetail(){
        MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
        when(merchantDetailServiceMock.getMerchantDetail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(dto);

        MerchantDetailDTO result = merchantService.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID);
        assertNotNull(result);
    }

    @Test
    void getMerchantDetailByMerchantIdAndInitiativeId(){
        MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);
        when(merchantDetailServiceMock.getMerchantDetail(Mockito.anyString(), Mockito.anyString())).thenReturn(merchantDetailDTO);

        MerchantDetailDTO result = merchantService.getMerchantDetail(MERCHANT_ID, INITIATIVE_ID);
        assertNotNull(result);
    }

    @Test
    void getMerchantList(){
        MerchantListDTO dto = new MerchantListDTO();
        when(merchantListServiceMock.getMerchantList(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), any())).thenReturn(dto);

        MerchantListDTO result = merchantService.getMerchantList(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID, null);
        assertNotNull(result);
    }

    @Test
    void retrieveMerchantId(){
        Merchant merchant = MerchantFaker.mockInstance(1);

        when(merchantRepositoryMock.retrieveByAcquirerIdAndFiscalCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Optional.of(merchant));

        String merchantIdOkResult = merchantService.retrieveMerchantId(merchant.getAcquirerId(), merchant.getFiscalCode());

        assertNotNull(merchantIdOkResult);
        Assertions.assertEquals(merchant.getMerchantId(), merchantIdOkResult);
    }

    @Test
    void retrieveMerchantId_NotFound(){

        doReturn(Optional.empty()).when(merchantRepositoryMock)
            .retrieveByAcquirerIdAndFiscalCode(any(), Mockito.eq("DUMMYFISCALCODE"));

        String merchantIdNotFoundResult= merchantService.retrieveMerchantId("DUMMYACQUIRERID", "DUMMYFISCALCODE");

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
    void updatingMerchantInitiative(){
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
}