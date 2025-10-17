package it.gov.pagopa.merchant.service.pointofsales;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import it.gov.pagopa.merchant.connector.payment.PaymentConnector;
import it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionDTO;
import it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO;
import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class PointOfSaleTransactionCheckServiceTest {

  @Mock
  private PaymentConnector paymentConnector;

  @Mock
  private TransactionConnector transactionConnector;

  PointOfSaleTransactionCheckService service;

  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String INITIATIVE_ID = "INITIATIVE_ID";

  @BeforeEach
  void setUp() {
    service = new PointOfSaleTransactionCheckServiceImpl(paymentConnector, transactionConnector);
  }


  @Test
  void hasInProgressTransactions_nullResult_returnsFalse() {
    when(paymentConnector.getPointOfSaleTransactions(eq(MERCHANT_ID), eq(INITIATIVE_ID),
        any(), any(), any(PageRequest.class)))
        .thenReturn(null);

    assertFalse(service.hasInProgressTransactions(MERCHANT_ID, INITIATIVE_ID));
  }

  @Test
  void hasInProgressTransactions_contentNull_returnsFalse() {
    MerchantTransactionsListDTO dto = new MerchantTransactionsListDTO();
    dto.setContent(null);

    when(paymentConnector.getPointOfSaleTransactions(eq(MERCHANT_ID), eq(INITIATIVE_ID),
        any(), any(), any(PageRequest.class)))
        .thenReturn(dto);

    assertFalse(service.hasInProgressTransactions(MERCHANT_ID, INITIATIVE_ID));
  }

  @Test
  void hasInProgressTransactions_withTransactions_returnsTrue() {
    MerchantTransactionDTO trx = new MerchantTransactionDTO();
    MerchantTransactionsListDTO dto = new MerchantTransactionsListDTO();
    dto.setContent(List.of(trx));

    when(paymentConnector.getPointOfSaleTransactions(eq(MERCHANT_ID), eq(INITIATIVE_ID),
        any(), any(), any(PageRequest.class)))
        .thenReturn(dto);

    assertTrue(service.hasInProgressTransactions(MERCHANT_ID, INITIATIVE_ID));
  }

  @Test
  void hasProcessedTransactions_withTransactions_returnsTrue() {
    it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionDTO trx = it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionDTO.builder().build();
    it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO dto = new it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO();
    dto.setContent(List.of(trx));

    when(transactionConnector.getPointOfSaleTransactions(eq(MERCHANT_ID), eq(INITIATIVE_ID),
        any(), any(), any(PageRequest.class)))
        .thenReturn(dto);

    assertTrue(service.hasProcessedTransactions(MERCHANT_ID, INITIATIVE_ID));
  }

  @Test
  void hasProcessedTransactions_nullResult_returnsFalse() {
    when(transactionConnector.getPointOfSaleTransactions(eq(MERCHANT_ID), eq(INITIATIVE_ID),
        any(), any(), any(PageRequest.class)))
        .thenReturn(null);

    assertFalse(service.hasProcessedTransactions(MERCHANT_ID, INITIATIVE_ID));
  }

  @Test
  void hasProcessedTransactions_contentNull_returnsFalse() {
    it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO dto = new it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO();
    dto.setContent(null);

    when(transactionConnector.getPointOfSaleTransactions(eq(MERCHANT_ID), eq(INITIATIVE_ID),
        any(), any(), any(PageRequest.class)))
        .thenReturn(dto);

    assertFalse(service.hasProcessedTransactions(MERCHANT_ID, INITIATIVE_ID));
  }
}
