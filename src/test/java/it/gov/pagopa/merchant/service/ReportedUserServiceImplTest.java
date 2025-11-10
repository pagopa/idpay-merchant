package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.transaction.RewardTransaction;
import it.gov.pagopa.merchant.mapper.ReportedUserMapper;
import it.gov.pagopa.merchant.model.ReportedUser;
import it.gov.pagopa.merchant.repository.ReportedUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static it.gov.pagopa.merchant.constants.MerchantConstants.TransactionStatus.ALLOWED_TRANSACTION_STATUSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportedUserServiceImplTest {

    @Mock private ReportedUserRepository repository;
    @Mock private ReportedUserMapper mapper;
    @Mock private TransactionConnector transactionConnector;
    @Mock private PDVService pdvService;

    @InjectMocks
    private ReportedUserServiceImpl service;

    @Captor
    private ArgumentCaptor<ReportedUser> reportedUserCaptor;

    private static final String MERCHANT_ID = "m-123";
    private static final String INITIATIVE_ID = "i-456";
    private static final String ENCRYPTED_USER_ID = "enc-uid-789";



    @Test
    void createReportedUser_ko_whenUserIdNullOrEmpty() {

        ReportedUserCreateResponseDTO res = service.createReportedUser(null, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isNotNull();
        verifyNoInteractions(transactionConnector);
        verify(repository, never()).save(any());
    }

    @Test
    void createReportedUser_ko_whenAlreadyReported() {
        when(repository.existsByUserId(ENCRYPTED_USER_ID)).thenReturn(true);

        ReportedUserCreateResponseDTO res = service.createReportedUser(ENCRYPTED_USER_ID, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isNotNull();
        verify(repository, never()).save(any());
        verifyNoInteractions(transactionConnector);
    }

    @Test
    void createReportedUser_ko_whenNoTransactionsFound() {
        when(repository.existsByUserId(ENCRYPTED_USER_ID)).thenReturn(false);

        when(transactionConnector.findAll(
                isNull(),
                eq(ENCRYPTED_USER_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                eq(PageRequest.of(0, 10))
        )).thenReturn(Collections.emptyList());

        ReportedUserCreateResponseDTO res = service.createReportedUser(ENCRYPTED_USER_ID, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isNotNull();
        verify(repository, never()).save(any());
    }


    @Test
    void createReportedUser_ok_whenMatchingTransactionExists() {
        when(repository.existsByUserId(ENCRYPTED_USER_ID)).thenReturn(false);

        RewardTransaction trx = mock(RewardTransaction.class);
        String allowed = ALLOWED_TRANSACTION_STATUSES.iterator().next();
        when(trx.getStatus()).thenReturn(allowed);
        when(trx.getInitiatives()).thenReturn(List.of("other", INITIATIVE_ID));
        when(trx.getMerchantId()).thenReturn(MERCHANT_ID);
        when(trx.getTrxChargeDate()).thenReturn(LocalDateTime.now().minusDays(3));
        when(trx.getId()).thenReturn("trx-001");

        when(transactionConnector.findAll(
                isNull(),
                eq(ENCRYPTED_USER_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                eq(PageRequest.of(0, 10))
        )).thenReturn(List.of(trx));

        when(repository.save(any(ReportedUser.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReportedUserCreateResponseDTO res = service.createReportedUser(ENCRYPTED_USER_ID, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isNotNull();
        verify(repository).save(reportedUserCaptor.capture());
        ReportedUser saved = reportedUserCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(ENCRYPTED_USER_ID);
        assertThat(saved.getMerchantId()).isEqualTo(MERCHANT_ID);
        assertThat(saved.getInitiativeId()).isEqualTo(INITIATIVE_ID);
        assertThat(saved.getTransactionId()).isEqualTo("trx-001");
        assertThat(saved.getTrxChargeDate()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void createReportedUser_ko_whenExternalConnectorThrows() {
        when(repository.existsByUserId(ENCRYPTED_USER_ID)).thenReturn(false);
        when(transactionConnector.findAll(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        ReportedUserCreateResponseDTO res = service.createReportedUser(ENCRYPTED_USER_ID, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isNotNull();
        verify(repository, never()).save(any());
    }

    @Test
    void searchReportedUser_empty_whenUserIdNullOrEmpty() {

        List<ReportedUserDTO> res = service.searchReportedUser(null, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isEmpty();
        verifyNoInteractions(repository, mapper);
    }

    @Test
    void searchReportedUser_empty_whenNotAlreadyReported() {
        when(repository.existsByUserId(ENCRYPTED_USER_ID)).thenReturn(false);

        List<ReportedUserDTO> res = service.searchReportedUser(ENCRYPTED_USER_ID, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isEmpty();
        verify(repository, never()).findByUserIdAndInitiativeIdAndMerchantId(any(), any(), any());
        verifyNoInteractions(mapper);
    }

    @Test
    void searchReportedUser_ok_returnsMappedList() {
        when(repository.existsByUserId(ENCRYPTED_USER_ID)).thenReturn(true);

        ReportedUser ru = ReportedUser.builder()
                .reportedUserId("rep-1")
                .userId(ENCRYPTED_USER_ID)
                .initiativeId(INITIATIVE_ID)
                .merchantId(MERCHANT_ID)
                .transactionId("trx-1")
                .trxChargeDate(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByUserIdAndInitiativeIdAndMerchantId(ENCRYPTED_USER_ID, INITIATIVE_ID, MERCHANT_ID))
                .thenReturn(List.of(ru));

        String fiscalCode = "RSSMRA80A01H501U";
        when(pdvService.decryptCF(ENCRYPTED_USER_ID)).thenReturn(fiscalCode);

        ReportedUserDTO dto = new ReportedUserDTO();
        dto.setFiscalCode(fiscalCode);
        when(mapper.toDtoList(List.of(ru), fiscalCode)).thenReturn(List.of(dto));

        List<ReportedUserDTO> res = service.searchReportedUser(ENCRYPTED_USER_ID, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).hasSize(1).containsExactly(dto);

        verify(repository).existsByUserId(ENCRYPTED_USER_ID);
        verify(pdvService).decryptCF(ENCRYPTED_USER_ID);
        verify(repository).findByUserIdAndInitiativeIdAndMerchantId(ENCRYPTED_USER_ID, INITIATIVE_ID, MERCHANT_ID);
        verify(mapper).toDtoList(List.of(ru), fiscalCode);
        verifyNoMoreInteractions(repository, pdvService, mapper);
    }


    @Test
    void deleteByUserId_ko_whenUserIdNullOrEmpty() {

        ReportedUserCreateResponseDTO res = service.deleteByUserId(null, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isNotNull();
        verify(repository, never()).deleteByUserIdAndInitiativeIdAndMerchantId(any(), any(), any());
    }

    @Test
    void deleteByUserId_ok_whenExists() {
        when(repository.existsByUserIdAndInitiativeIdAndMerchantId(ENCRYPTED_USER_ID, INITIATIVE_ID, MERCHANT_ID))
                .thenReturn(true);

        ReportedUserCreateResponseDTO res = service.deleteByUserId(ENCRYPTED_USER_ID, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isNotNull();
        verify(repository).deleteByUserIdAndInitiativeIdAndMerchantId(ENCRYPTED_USER_ID, INITIATIVE_ID, MERCHANT_ID);
    }

    @Test
    void deleteByUserId_ko_whenNotExists() {
        when(repository.existsByUserIdAndInitiativeIdAndMerchantId(ENCRYPTED_USER_ID, INITIATIVE_ID, MERCHANT_ID))
                .thenReturn(false);

        ReportedUserCreateResponseDTO res = service.deleteByUserId(ENCRYPTED_USER_ID, MERCHANT_ID, INITIATIVE_ID);

        assertThat(res).isNotNull();
        verify(repository, never()).deleteByUserIdAndInitiativeIdAndMerchantId(any(), any(), any());
    }
}