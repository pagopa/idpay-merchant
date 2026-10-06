package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.connector.payment.PaymentConnector;
import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionDTO;
import it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.enums.PosOnbordingExclusionRejectionReason;
import it.gov.pagopa.merchant.dto.enums.PosOnbordingRejectionReason;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.exception.custom.InitiativeNotValidException;
import it.gov.pagopa.merchant.exception.custom.MerchantNotAllowedException;
import it.gov.pagopa.merchant.exception.custom.PosValidationException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.repository.PointOfSalesInitiativeRepository;
import it.gov.pagopa.merchant.service.KeycloakService;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointOfSaleWriterTest {

    @Mock
    MerchantService merchantService;
    @Mock
    PointOfSaleRepository repository;
    @Mock
    KeycloakService keycloakService;
    @Mock
    PointOfSaleDTOMapper mapper;
    @Mock
    PointOfSalesInitiativeRepository initiativeRepository;
    @Mock
    TransactionConnector transactionConnector;
    @Mock
    PaymentConnector paymentConnector;

    PointOfSaleWriterImpl service;

    static final String M = "M1";
    static final String I = "I1";

    @BeforeEach
    void setUp() {
        service = new PointOfSaleWriterImpl(
                merchantService,
                repository,
                keycloakService,
                mapper,
                initiativeRepository,
                transactionConnector,
                paymentConnector
        );
    }

    private PointOfSaleDTO dto() {
        return PointOfSaleDTOFaker.mockInstance();
    }

    private PointOfSale entity(String id) {
        PointOfSale p = PointOfSaleFaker.mockInstance();
        p.setId(id);
        return p;
    }


    @Test
    void shouldSaveSuccessfully () {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenReturn(entity);

        service.savePointOfSales(M, I, list);

        verify(repository).save(entity);
        verify(keycloakService).manageReferentUserOnKeycloak(entity, dto.getContactEmail());
        verify(initiativeRepository).saveAll(any());
    }

    @Test
    void shouldFail_whenEmailAlreadyExists () {
        PointOfSaleDTO dto = dto();
        PointOfSale existing = entity("X");
        List<PointOfSaleDTO> list = List.of(dto);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto, M)).thenReturn(existing);
        when(repository.findByContactEmail(any())).thenReturn(Optional.of(existing));

        PosValidationException ex = assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        assertFalse(ex.getErrors().isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFail_whenOnlinePosAlreadyOnSameInitiative () {
        PointOfSaleDTO dto = dto();
        dto.setType(PointOfSaleTypeEnum.ONLINE);
        dto.setWebsite("https://test.it");

        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.of(entity));

        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                        entity.getId(), I, M))
                .thenReturn(Optional.of(new PointOfSalesInitiative()));

        PosValidationException ex = assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        assertTrue(ex.getErrors().stream().anyMatch(e ->
                e.getCode().equals(PointOfSaleConstants.CODE_ONLINE_POS_ALREADY_REGISTERED)));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFail_whenPhysicalPosAlreadyOnSameInitiative () {
        PointOfSaleDTO dto = dto();
        dto.setType(PointOfSaleTypeEnum.PHYSICAL);
        dto.setAddress("Via Roma");

        PointOfSale entity = entity("P2");
        List<PointOfSaleDTO> list = List.of(dto);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.of(entity));

        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                        entity.getId(), I, M))
                .thenReturn(Optional.of(new PointOfSalesInitiative()));

        PosValidationException ex = assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        assertTrue(ex.getErrors().stream().anyMatch(e ->
                e.getCode().equals(PointOfSaleConstants.CODE_PHYSICAL_POS_ALREADY_REGISTERED)));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFail_whenPosAlreadyOnAnotherInitiative () {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P3");
        List<PointOfSaleDTO> list = List.of(dto);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.of(entity));

        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                        entity.getId(), I, M))
                .thenReturn(Optional.empty());

        PosValidationException ex = assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        assertTrue(ex.getErrors().stream().anyMatch(e ->
                e.getCode().equals(PointOfSaleConstants.CODE_POS_ALREADY_REGISTERED_OTHER_INITIATIVE)));
    }

    @Test
    void shouldReturnMultipleErrors () {
        PointOfSaleDTO dto1 = dto();
        PointOfSaleDTO dto2 = dto();

        PointOfSale e1 = entity("1");
        PointOfSale e2 = entity("2");

        List<PointOfSaleDTO> list = List.of(dto1, dto2);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto1, M)).thenReturn(e1);
        when(mapper.dtoToEntity(dto2, M)).thenReturn(e2);

        when(repository.findByContactEmail(any()))
                .thenReturn(Optional.of(e1));

        assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowServiceException_onDuplicateKey () {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenThrow(new DuplicateKeyException("dup"));

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));
    }

    @Test
    void shouldThrowServiceException_onGenericError () {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenThrow(new RuntimeException());

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));
    }

    @Test
    void shouldCompensate_whenAssociationSaveFails(){
        PointOfSaleDTO dto = dto();
        dto.setContactEmail("test@email.it");

        PointOfSale entity = entity("P1");
        entity.setContactEmail("test@email.it");

        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenReturn(entity);

        doThrow(new RuntimeException())
                .when(initiativeRepository).saveAll(any());

        UsersResource usersResource = mock(UsersResource.class);
        when(keycloakService.getUserResource()).thenReturn(usersResource);

        UserRepresentation user = new UserRepresentation();
        user.setId("USER_ID");

        var userResource = mock(org.keycloak.admin.client.resource.UserResource.class);

        when(usersResource.searchByEmail("test@email.it", true))
                .thenReturn(List.of(user));

        when(usersResource.get("USER_ID"))
                .thenReturn(userResource);

        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));

        verify(initiativeRepository)
                .deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(eq(M), eq(I), any());

        verify(repository).deleteById("P1");

        verify(usersResource).searchByEmail("test@email.it", true);
        verify(userResource).remove();
    }



    @Test
    void shouldContinueCompensation_whenKeycloakFails() {

        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenReturn(entity);

        doThrow(new RuntimeException())
                .when(initiativeRepository).saveAll(any());

        UsersResource usersResource = mock(UsersResource.class);
        when(keycloakService.getUserResource()).thenReturn(usersResource);

        when(usersResource.searchByEmail(anyString(), eq(true)))
                .thenThrow(new RuntimeException());

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));


        verify(repository).deleteById(entity.getId());
    }

    @Test
    void shouldContinueCompensation_whenDeleteFails() {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");

        dto.setContactEmail("test@email.it");
        entity.setContactEmail("test@email.it");

        List<PointOfSaleDTO> list = List.of(dto);
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        doThrow(new RuntimeException())
                .when(initiativeRepository).saveAll(any());

        doThrow(new RuntimeException())
                .when(repository).deleteById("P1");


        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));

        verify(repository).deleteById("P1");
    }

    private Merchant buildMerchant(boolean validInitiative, boolean expired) {
        Initiative initiative = new Initiative();
        initiative.setInitiativeId(I);
        initiative.setEndDate(expired
                ? LocalDate.now().minusDays(1)
                : LocalDate.now().plusDays(10));

        Merchant merchant = new Merchant();
        if (validInitiative) {
            merchant.setInitiativeList(List.of(initiative));
        } else {
            merchant.setInitiativeList(List.of());
        }

        return merchant;
    }

    private PointOfSale pos() {
        PointOfSale p = new PointOfSale();
        p.setId("P1");
        p.setMerchantId(M);
        p.setFranchiseName("SHOP");
        p.setAddress("ADDR");
        p.setCity("CITY");
        p.setStreetNumber("1");
        return p;
    }

    @Test
    void shouldThrow_whenMerchantNotOnboardedOnInitiative() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(false, false));

        assertThrows(MerchantNotAllowedException.class,
                () -> service.onboardingPointOfSales(M, I, List.of("P1")));
    }

    @Test
    void shouldThrow_whenInitiativeEnded() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, true));

        assertThrows(InitiativeNotValidException.class,
                () -> service.onboardingPointOfSales(M, I, List.of("P1")));
    }

    @Test
    void shouldAddNotAssociated_whenPosNotFound() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        when(repository.findById("P1"))
                .thenReturn(Optional.empty());

        var result = service.onboardingPointOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getNotAssociated().size());
        assertEquals(PosOnbordingRejectionReason.NOT_FOUND,
                result.getNotAssociated().getFirst().getReason());
    }

    @Test
    void shouldAddNotAssociated_whenInvalidMerchant() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        PointOfSale pos = pos();
        pos.setMerchantId("OTHER");
        pos.setType(String.valueOf(PointOfSaleTypeEnum.PHYSICAL));
        when(repository.findById("P1"))
                .thenReturn(Optional.of(pos));

        var result = service.onboardingPointOfSales(M, I, List.of("P1"));

        assertEquals(PosOnbordingRejectionReason.INVALID,
                result.getNotAssociated().getFirst().getReason());
    }

    @Test
    void shouldAddNotAssociated_whenAlreadyAssociated() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        PointOfSale pos = pos();
        pos.setType(String.valueOf(PointOfSaleTypeEnum.PHYSICAL));
        when(repository.findById("P1"))
                .thenReturn(Optional.of(pos));

        PointOfSalesInitiative enabledAssoc = new PointOfSalesInitiative();
        enabledAssoc.setEnabled(true);
        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantId("P1", I, M))
                .thenReturn(Optional.of(enabledAssoc));

        var result = service.onboardingPointOfSales(M, I, List.of("P1"));

        assertEquals(PosOnbordingRejectionReason.ALREADY_ASSOCIATED,
                result.getNotAssociated().getFirst().getReason());
    }

    @Test
    void shouldReEnable_whenAssociationExistsButDisabled() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        PointOfSale pos = pos();
        when(repository.findById("P1"))
                .thenReturn(Optional.of(pos));

        PointOfSalesInitiative disabledAssoc = new PointOfSalesInitiative();
        disabledAssoc.setEnabled(false);
        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantId("P1", I, M))
                .thenReturn(Optional.of(disabledAssoc));

        var result = service.onboardingPointOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getAssociated().size());
        assertTrue(result.getNotAssociated().isEmpty());
        assertTrue(disabledAssoc.getEnabled());
        assertNotNull(disabledAssoc.getUpdatedAt());
        verify(initiativeRepository).save(disabledAssoc);
    }

    @Test
    void shouldAssociateSuccessfully() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        PointOfSale pos = pos();

        when(repository.findById("P1"))
                .thenReturn(Optional.of(pos));

        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantId("P1", I, M))
                .thenReturn(Optional.empty());

        var result = service.onboardingPointOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getAssociated().size());
        assertTrue(result.getNotAssociated().isEmpty());

        verify(initiativeRepository).save(any());
    }

    @Test
    void shouldHandleGenericException() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        when(repository.findById("P1"))
                .thenThrow(new RuntimeException());

        var result = service.onboardingPointOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getNotAssociated().size());
        assertEquals(PosOnbordingRejectionReason.GENERIC_ERROR,
                result.getNotAssociated().getFirst().getReason());
    }

    @Test
    void shouldHandleMixedResults() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        PointOfSale valid = pos();

        when(repository.findById("P1"))
                .thenReturn(Optional.of(valid));

        when(repository.findById("P2"))
                .thenReturn(Optional.empty());

        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantId("P1", I, M))
                .thenReturn(Optional.empty());

        var result = service.onboardingPointOfSales(M, I, List.of("P1", "P2"));

        assertEquals(1, result.getAssociated().size());
        assertEquals(1, result.getNotAssociated().size());
    }


    @Test
    void excludePointsOfSales_shouldThrow_whenMerchantNotOnboardedOnInitiative() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(false, false));

        List<String> pointsOfSaleIds = List.of("P1");

        assertThrows(MerchantNotAllowedException.class,
                () -> service.excludePointsOfSales(M, I, pointsOfSaleIds));

        verifyNoInteractions(paymentConnector, transactionConnector, repository, initiativeRepository);
    }

    @Test
    void excludePointsOfSales_shouldThrow_whenInitiativeEnded() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, true));

        List<String> pointsOfSaleIds = List.of("P1");

        assertThrows(InitiativeNotValidException.class,
                () -> service.excludePointsOfSales(M, I, pointsOfSaleIds));

        verifyNoInteractions(paymentConnector, transactionConnector, repository, initiativeRepository);
    }

    @Test
    void excludePointsOfSales_shouldAddNotExcluded_whenPosNotFound() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));
        when(repository.findById("P1"))
                .thenReturn(Optional.empty());

        var result = service.excludePointsOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getNotExcludedPointOfSales().size());
        assertEquals("P1", result.getNotExcludedPointOfSales().getFirst().getPointOfSaleId());
        assertEquals(PosOnbordingExclusionRejectionReason.NOT_FOUND, result.getNotExcludedPointOfSales().getFirst().getReason());
        assertTrue(result.getExcludedPointOfSales().isEmpty());
        verify(initiativeRepository, never()).save(any());
        // No transaction lookup is performed when the POS does not exist
        verifyNoInteractions(paymentConnector, transactionConnector);
    }

    @Test
    void excludePointsOfSales_shouldAddNotExcluded_whenAlreadyExcludedOrNeverEnabled() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        PointOfSale pos = pos();
        when(repository.findById("P1")).thenReturn(Optional.of(pos));
        when(initiativeRepository.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue("P1", I, M))
                .thenReturn(Optional.empty());

        var result = service.excludePointsOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getNotExcludedPointOfSales().size());
        assertEquals(PosOnbordingExclusionRejectionReason.ALREADY_EXCLUDED, result.getNotExcludedPointOfSales().getFirst().getReason());
        assertTrue(result.getExcludedPointOfSales().isEmpty());
        verify(initiativeRepository, never()).save(any());
        // No transaction lookup is performed when the POS is already excluded
        verifyNoInteractions(paymentConnector, transactionConnector);
    }

    @Test
    void excludePointsOfSales_shouldExcludeSuccessfully() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        // No blocking transactions on either source for this POS
        when(paymentConnector.getMerchantTransactions(eq(M), eq(I), isNull(), isNull(), eq("P1"), any()))
                .thenReturn(new it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO());
        when(transactionConnector.getMerchantTransactions(eq(M), eq(I), isNull(), isNull(), eq("P1"), any()))
                .thenReturn(new MerchantTransactionsListDTO());

        PointOfSale pos = pos();
        when(repository.findById("P1")).thenReturn(Optional.of(pos));

        PointOfSalesInitiative association = new PointOfSalesInitiative();
        association.setEnabled(true);
        when(initiativeRepository.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue("P1", I, M))
                .thenReturn(Optional.of(association));

        var result = service.excludePointsOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getExcludedPointOfSales().size());
        assertEquals("P1", result.getExcludedPointOfSales().getFirst().getPointOfSaleId());
        assertEquals("SHOP", result.getExcludedPointOfSales().getFirst().getFranchiseName());
        assertTrue(result.getNotExcludedPointOfSales().isEmpty());

        assertFalse(association.getEnabled());
        assertNotNull(association.getUpdatedAt());
        verify(initiativeRepository).save(association);
    }

    @Test
    void excludePointsOfSales_shouldHandleGenericException_InsideLoop() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        when(repository.findById("P1")).thenThrow(new RuntimeException("DB offline"));

        var result = service.excludePointsOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getNotExcludedPointOfSales().size());
        assertEquals(PosOnbordingExclusionRejectionReason.GENERIC_ERROR, result.getNotExcludedPointOfSales().getFirst().getReason());
        assertTrue(result.getExcludedPointOfSales().isEmpty());
    }

    @Test
    void excludePointsOfSales_shouldHandleMixedResults() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        // P1 is excludable: no blocking transactions
        when(paymentConnector.getMerchantTransactions(eq(M), eq(I), isNull(), isNull(), eq("P1"), any()))
                .thenReturn(new it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO());
        when(transactionConnector.getMerchantTransactions(eq(M), eq(I), isNull(), isNull(), eq("P1"), any()))
                .thenReturn(new MerchantTransactionsListDTO());

        PointOfSale p1 = pos();
        p1.setId("P1");
        PointOfSalesInitiative assocP1 = new PointOfSalesInitiative();
        assocP1.setEnabled(true);
        when(repository.findById("P1")).thenReturn(Optional.of(p1));
        when(initiativeRepository.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue("P1", I, M))
                .thenReturn(Optional.of(assocP1));

        PointOfSale p2 = pos();
        p2.setId("P2");
        when(repository.findById("P2")).thenReturn(Optional.of(p2));
        when(initiativeRepository.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue("P2", I, M))
                .thenReturn(Optional.empty());

        var result = service.excludePointsOfSales(M, I, List.of("P1", "P2"));

        assertEquals(1, result.getExcludedPointOfSales().size());
        assertEquals("P1", result.getExcludedPointOfSales().getFirst().getPointOfSaleId());

        assertEquals(1, result.getNotExcludedPointOfSales().size());
        assertEquals("P2", result.getNotExcludedPointOfSales().getFirst().getPointOfSaleId());
        assertEquals(PosOnbordingExclusionRejectionReason.ALREADY_EXCLUDED, result.getNotExcludedPointOfSales().getFirst().getReason());

        verify(initiativeRepository, times(1)).save(any());
    }

    @Test
    void excludePointsOfSales_shouldAddNotExcluded_whenHasProcessedTransactions() {
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        // No in-progress transactions, but a processed one exists for this POS
        when(paymentConnector.getMerchantTransactions(eq(M), eq(I), isNull(), isNull(), eq("P1"), any()))
                .thenReturn(new it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO());

        MerchantTransactionDTO processedTrx = MerchantTransactionDTO.builder().pointOfSaleId("P1").build();
        MerchantTransactionsListDTO processedList = MerchantTransactionsListDTO.builder()
                .content(List.of(processedTrx))
                .build();
        when(transactionConnector.getMerchantTransactions(eq(M), eq(I), isNull(), isNull(), eq("P1"), eq(PageRequest.of(0, 1))))
                .thenReturn(processedList);

        PointOfSale pos = pos();
        when(repository.findById("P1")).thenReturn(Optional.of(pos));
        when(initiativeRepository.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue("P1", I, M))
                .thenReturn(Optional.of(new PointOfSalesInitiative()));

        var result = service.excludePointsOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getNotExcludedPointOfSales().size());
        assertEquals(PosOnbordingExclusionRejectionReason.HAS_TRANSACTIONS, result.getNotExcludedPointOfSales().getFirst().getReason());
        assertTrue(result.getExcludedPointOfSales().isEmpty());
        verify(initiativeRepository, never()).save(any());
        // Existence check is scoped to the single POS and asks for one row only
        verify(transactionConnector).getMerchantTransactions(eq(M), eq(I), isNull(), isNull(), eq("P1"), eq(PageRequest.of(0, 1)));
    }

    @Test
    void excludePointsOfSales_shouldAddNotExcluded_whenHasInProgressPaymentTransactions() {
        // Reproduces bug: POS with only in-progress payment transactions ("Da autorizzare" / "Fattura da caricare")
        when(merchantService.getMerchantByMerchantId(M))
                .thenReturn(buildMerchant(true, false));

        // In-progress transactions (idpay-payment) reference the POS -> must block exclusion.
        // Payment is checked first and short-circuits, so the processed source is not queried.
        it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionDTO inProgressTrx =
                it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionDTO.builder()
                        .pointOfSaleId("P1")
                        .status(it.gov.pagopa.merchant.connector.payment.dto.SyncTrxStatus.AUTHORIZED)
                        .build();
        it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO paymentList =
                it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO.builder()
                        .content(List.of(inProgressTrx))
                        .build();
        when(paymentConnector.getMerchantTransactions(eq(M), eq(I), isNull(), isNull(), eq("P1"), any()))
                .thenReturn(paymentList);

        PointOfSale pos = pos();
        when(repository.findById("P1")).thenReturn(Optional.of(pos));
        when(initiativeRepository.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue("P1", I, M))
                .thenReturn(Optional.of(new PointOfSalesInitiative()));

        var result = service.excludePointsOfSales(M, I, List.of("P1"));

        assertEquals(1, result.getNotExcludedPointOfSales().size());
        assertEquals(PosOnbordingExclusionRejectionReason.HAS_TRANSACTIONS, result.getNotExcludedPointOfSales().getFirst().getReason());
        assertTrue(result.getExcludedPointOfSales().isEmpty());
        verify(initiativeRepository, never()).save(any());
        verify(transactionConnector, never()).getMerchantTransactions(any(), any(), any(), any(), any(), any());
    }
}
