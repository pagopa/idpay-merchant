package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.dto.*;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

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

    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
    private static final String ACQUIRER_ID = "PAGOPA";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String OPERATION_TYPE_DELETE_INITIATIVE = "DELETE_INITIATIVE";
    private final Initiative2InitiativeDTOMapper initiative2InitiativeDTOMapper = new Initiative2InitiativeDTOMapper();

    private MerchantServiceImpl merchantService;

    @BeforeEach
    void setUp(){
        merchantService = new MerchantServiceImpl(
                merchantDetailServiceMock,
                merchantListServiceMock,
                merchantProcessOperationService, merchantUpdatingInitiativeService, merchantUpdateIbanService, merchantRepositoryMock,
                uploadingMerchantServiceMock,
                initiative2InitiativeDTOMapper);
    }

    @AfterEach
    void verifyNoMoreMockInteractions() {
        Mockito.verifyNoMoreInteractions(
                merchantDetailServiceMock,
                merchantListServiceMock,
                merchantRepositoryMock);
    }

    @Test
    void uploadMerchantFile () {
        MerchantUpdateDTO merchantUpdateDTO = MerchantUpdateDTOFaker.mockInstance(1);
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "Content".getBytes());
        Mockito.when(merchantService.uploadMerchantFile(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(merchantUpdateDTO);

        MerchantUpdateDTO result = merchantService.uploadMerchantFile(file, ORGANIZATION_ID, INITIATIVE_ID, "ORGANIZATION_USER_ID", ACQUIRER_ID);
        Assertions.assertNotNull(result);
    }
    @Test
    void getMerchantDetail(){
        MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
        Mockito.when(merchantService.getMerchantDetail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(dto);

        MerchantDetailDTO result = merchantService.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID);
        assertNotNull(result);
    }

    @Test
    void getMerchantDetailByMerchantIdAndInitiativeId(){
        MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);
        Mockito.when(merchantService.getMerchantDetail(Mockito.anyString(), Mockito.anyString())).thenReturn(merchantDetailDTO);

        MerchantDetailDTO result = merchantService.getMerchantDetail(MERCHANT_ID, INITIATIVE_ID);
        assertNotNull(result);
    }

    @Test
    void getMerchantList(){
        MerchantListDTO dto = new MerchantListDTO();
        Mockito.when(merchantService.getMerchantList(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(dto);

        MerchantListDTO result = merchantService.getMerchantList(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID, null);
        assertNotNull(result);
    }

    @Test
    void retrieveMerchantId(){
        Merchant merchant = MerchantFaker.mockInstance(1);

        Mockito.when(merchantRepositoryMock.retrieveByAcquirerIdAndFiscalCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Optional.of(merchant));

        String merchantIdOkResult = merchantService.retrieveMerchantId(merchant.getAcquirerId(), merchant.getFiscalCode());

        assertNotNull(merchantIdOkResult);
        Assertions.assertEquals(merchant.getMerchantId(), merchantIdOkResult);
    }

    @Test
    void retrieveMerchantId_NotFound(){

        doReturn(Optional.empty()).when(merchantRepositoryMock)
                .retrieveByAcquirerIdAndFiscalCode(Mockito.any(), Mockito.eq("DUMMYFISCALCODE"));

        String merchantIdNotFoundResult= merchantService.retrieveMerchantId("DUMMYACQUIRERID", "DUMMYFISCALCODE");

        assertNull(merchantIdNotFoundResult);
        Mockito.verify(merchantRepositoryMock).retrieveByAcquirerIdAndFiscalCode(Mockito.anyString(), Mockito.anyString());
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

        Mockito.verify(merchantProcessOperationService).processOperation(queueCommandOperationDTO);
    }

    @Test
    void updatingMerchantInitiative(){
        QueueInitiativeDTO queueInitiativeDTO = QueueInitiativeDTO.builder()
                .initiativeId(INITIATIVE_ID)
                .initiativeRewardType("DISCOUNT")
                .build();

        merchantService.updatingInitiative(queueInitiativeDTO);
        Mockito.verify(merchantUpdatingInitiativeService, Mockito.times(1)).updatingInitiative(queueInitiativeDTO);
    }

    @Test
    void updateIban_success() {
        // Given
        IbanPutDTO ibanPutDTO = new IbanPutDTO("NEW_IBAN", "NEW_HOLDER");
        Initiative initiative = InitiativeFaker.mockInstanceBuilder(1)
            .initiativeId(INITIATIVE_ID)
            .organizationId(ORGANIZATION_ID)
            .build();
        Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
            .merchantId(MERCHANT_ID)
            .initiativeList(List.of(initiative))
            .build();
        MerchantDetailDTO expectedDto = MerchantDetailDTOFaker.mockInstance(1);

        when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(merchantDetailServiceMock.getMerchantDetail(ORGANIZATION_ID, INITIATIVE_ID, MERCHANT_ID)).thenReturn(expectedDto);

        // When
        MerchantDetailDTO result = merchantService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, ibanPutDTO);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto, result);

        ArgumentCaptor<Merchant> captor = ArgumentCaptor.forClass(Merchant.class);
        Mockito.verify(merchantRepositoryMock).save(captor.capture());
        Merchant savedMerchant = captor.getValue();
        assertEquals("NEW_IBAN", savedMerchant.getIban());
        assertEquals("NEW_HOLDER", savedMerchant.getHolder());
    }

    @Test
    void updateIban_merchantNotFound() {
        // Given
        IbanPutDTO ibanPutDTO = new IbanPutDTO("NEW_IBAN", "NEW_HOLDER");
        when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MerchantNotFoundException.class,
            () -> merchantService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, ibanPutDTO));
    }

    @Test
    void updateIban_initiativeNotFoundForMerchant() {
        // Given
        IbanPutDTO ibanPutDTO = new IbanPutDTO("NEW_IBAN", "NEW_HOLDER");
        Merchant merchant = MerchantFaker.mockInstanceBuilder(1)
            .merchantId(MERCHANT_ID)
            .initiativeList(Collections.emptyList()) // No initiatives
            .build();

        when(merchantRepositoryMock.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));

        // When & Then
        assertThrows(MerchantNotFoundException.class,
            () -> merchantService.updateIban(MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID, ibanPutDTO));
    }
}