package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.dto.transaction.RewardTransaction;
import it.gov.pagopa.merchant.mapper.ReportedUserMapper;
import it.gov.pagopa.merchant.model.ReportedUser;
import it.gov.pagopa.merchant.repository.ReportedUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ReportedUserServiceImplTest {

    @Mock private ReportedUserRepository repository;
    @Mock private PDVService pdvService;
    @Mock private ReportedUserMapper mapper;
    @Mock private TransactionConnector transactionConnector;

    private ReportedUserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReportedUserServiceImpl(repository, pdvService, mapper, transactionConnector);
    }


    @Test
    void createReportedUser_whenUserIdNull_returnsKoUserIdNotFound() {
        ReportedUserRequestDTO req = baseRequest();

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn(null);

        ReportedUserCreateResponseDTO res = service.createReportedUser(req);

        assertNotNull(res);
        verify(repository, never()).existsByUserId(anyString());
        verifyNoInteractions(transactionConnector, mapper, repository);
    }

    @Test
    void createReportedUser_whenUserAlreadyReported_returnsKoAlreadyReported() {
        ReportedUserRequestDTO req = baseRequest();

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(true);

        ReportedUserCreateResponseDTO res = service.createReportedUser(req);

        assertNotNull(res);
        verify(repository).existsByUserId("encUser");
        verify(repository, never()).save(any());
        verifyNoInteractions(transactionConnector, mapper);
    }

    @Test
    void createReportedUser_whenTrxNull_returnsKoEntityNotFound() {
        ReportedUserRequestDTO req = baseRequest();

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(false);
        when(transactionConnector.findAll(
                isNull(),
                eq("encUser"),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                eq(PageRequest.of(0, 10))
        )).thenReturn(null);

        ReportedUserCreateResponseDTO res = service.createReportedUser(req);

        assertNotNull(res);
        verify(repository, never()).save(any());
    }

    @Test
    void createReportedUser_whenTrxInitiativesEmpty_returnsKoEntityNotFound() {
        ReportedUserRequestDTO req = baseRequest();

        RewardTransaction trx = mock(RewardTransaction.class);
        when(trx.getInitiatives()).thenReturn(new ArrayList<>());

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(false);
        when(transactionConnector.findAll(any(), eq("encUser"),
                any(), any(), any(), any())).thenReturn(trx);

        ReportedUserCreateResponseDTO res = service.createReportedUser(req);

        assertNotNull(res);
        verify(repository, never()).save(any());
    }

    @Test
    void createReportedUser_whenDifferentMerchant_returnsKoDifferentMerchantId() {
        ReportedUserRequestDTO req = baseRequest();

        RewardTransaction trx = mock(RewardTransaction.class);
        when(trx.getInitiatives()).thenReturn(List.of("INIT-1"));
        when(trx.getMerchantId()).thenReturn("OTHER-MERCH");

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(false);
        when(transactionConnector.findAll(any(), eq("encUser"),
                any(), any(), any(), any())).thenReturn(trx);

        ReportedUserCreateResponseDTO res = service.createReportedUser(req);

        assertNotNull(res);
        verify(repository, never()).save(any());
    }

    @Test
    void createReportedUser_whenDifferentInitiative_returnsKoDifferentInitiativeId() {
        ReportedUserRequestDTO req = baseRequest();

        RewardTransaction trx = mock(RewardTransaction.class);
        when(trx.getInitiatives()).thenReturn(List.of("OTHER-INIT"));
        when(trx.getMerchantId()).thenReturn("MERCH-1");

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(false);
        when(transactionConnector.findAll(any(), eq("encUser"),
                any(), any(), any(), any())).thenReturn(trx);

        ReportedUserCreateResponseDTO res = service.createReportedUser(req);

        assertNotNull(res);
        verify(repository, never()).save(any());
    }

    @Test
    void createReportedUser_success_savesAndReturnsOk() {
        ReportedUserRequestDTO req = baseRequest();

        RewardTransaction trx = mock(RewardTransaction.class);
        when(trx.getInitiatives()).thenReturn(List.of("INIT-1"));
        when(trx.getMerchantId()).thenReturn("MERCH-1");
        when(trx.getId()).thenReturn("TRX-ID-1");
        LocalDateTime trxDate = LocalDateTime.of(2024, 6, 10, 12, 0, 0);
        when(trx.getTrxDate()).thenReturn(trxDate);

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(false);

        when(transactionConnector.findAll(
                isNull(),
                eq("encUser"),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                eq(PageRequest.of(0, 10))
        )).thenReturn(trx);

        ReportedUser mapped = ReportedUser.builder().build();
        when(mapper.fromRequestDtoToEntity(req)).thenReturn(mapped);

        ReportedUser saved = ReportedUser.builder().reportedUserId("RID-1").build();
        when(repository.save(any(ReportedUser.class))).thenReturn(saved);

        ReportedUserCreateResponseDTO res = service.createReportedUser(req);

        assertNotNull(res);
        verify(mapper).fromRequestDtoToEntity(req);
        verify(repository).save(argThat(e ->
                "TRX-ID-1".equals(e.getTransactionId())
                        && trxDate.equals(e.getTransactionDate())
                        && "encUser".equals(e.getUserId())
                        && "INIT-1".equals(e.getInitiativeId())
                        && "MERCH-1".equals(e.getMerchantId())
                        && e.getCreatedAt() != null
        ));
    }


    @Test
    void createReportedUser_whenConnectorThrows_returnsServiceUnavailable() {
        ReportedUserRequestDTO req = baseRequest();

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(false);
        when(transactionConnector.findAll(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        ReportedUserCreateResponseDTO res = service.createReportedUser(req);

        assertNotNull(res);
        verify(repository, never()).save(any());
    }


    @Test
    void searchReportedUser_whenUserIdEmpty_returnsEmptyList() {
        ReportedUserRequestDTO req = baseRequest();

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("");

        List<ReportedUserDTO> out = service.searchReportedUser(req);

        assertNotNull(out);
        assertTrue(out.isEmpty());
        verifyNoInteractions(repository, mapper);
    }

    @Test
    void searchReportedUser_whenNotReported_returnsEmptyList() {
        ReportedUserRequestDTO req = baseRequest();

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(false);

        List<ReportedUserDTO> out = service.searchReportedUser(req);

        assertNotNull(out);
        assertTrue(out.isEmpty());
        verify(repository).existsByUserId("encUser");
        verify(repository, never()).findByUserId(anyString());
        verifyNoInteractions(mapper);
    }

    @Test
    void searchReportedUser_success_returnsMappedList() {
        ReportedUserRequestDTO req = baseRequest();

        when(pdvService.encryptCF(req.getUserFiscalCode())).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(true);

        List<ReportedUser> entities = List.of(
                ReportedUser.builder().transactionId("T1").build(),
                ReportedUser.builder().transactionId("T2").build()
        );
        when(repository.findByUserId("encUser")).thenReturn(entities);

        List<ReportedUserDTO> mapped = List.of(
                ReportedUserDTO.builder().transactionId("T1").fiscalCode(req.getUserFiscalCode()).build(),
                ReportedUserDTO.builder().transactionId("T2").fiscalCode(req.getUserFiscalCode()).build()
        );
        when(mapper.toDtoList(entities, req.getUserFiscalCode())).thenReturn(mapped);

        List<ReportedUserDTO> out = service.searchReportedUser(req);

        assertEquals(mapped, out);
        verify(mapper).toDtoList(entities, req.getUserFiscalCode());
    }


    @Test
    void deleteByUserId_whenUserIdNull_returnsKoUserIdNotFound() {
        String req = "RSSMRA80A01H501U";

        when(pdvService.encryptCF(req)).thenReturn(null);

        ReportedUserCreateResponseDTO res = service.deleteByUserId(req);

        assertNotNull(res);
        verifyNoInteractions(repository);
    }

    @Test
    void deleteByUserId_whenExists_deletesAndOk() {
        String req = "RSSMRA80A01H501U";

        when(pdvService.encryptCF(req)).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(true);

        ReportedUserCreateResponseDTO res = service.deleteByUserId(req);

        assertNotNull(res);
        verify(repository).deleteById("encUser");
    }

    @Test
    void deleteByUserId_whenNotExists_returnsKoEntityNotFound() {
        String req = "RSSMRA80A01H501U";

        when(pdvService.encryptCF(req)).thenReturn("encUser");
        when(repository.existsByUserId("encUser")).thenReturn(false);

        ReportedUserCreateResponseDTO res = service.deleteByUserId(req);

        assertNotNull(res);
        verify(repository, never()).deleteById(anyString());
    }

    private ReportedUserRequestDTO baseRequest() {
        return ReportedUserRequestDTO.builder()
                .merchantId("MERCH-1")
                .userFiscalCode("RSSMRA80A01H501U")
                .initiativeId("INIT-1")
                .build();
    }
}
