package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.connector.payment.PaymentConnector;
import it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO;
import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class PointOfSaleTransactionCheckServiceImpl implements PointOfSaleTransactionCheckService {

  private final PaymentConnector paymentConnector;
  private final TransactionConnector transactionConnector;

  public PointOfSaleTransactionCheckServiceImpl(PaymentConnector paymentConnector,
      TransactionConnector transactionConnector) {
    this.paymentConnector = paymentConnector;
    this.transactionConnector = transactionConnector;
  }

  @Override
  public boolean hasInProgressTransactions(String merchantId, String initiativeId) {

      try {
        MerchantTransactionsListDTO inProgressTrx = paymentConnector.getPointOfSaleTransactions(merchantId, initiativeId, null, null, PageRequest.of(0,1));

        if (inProgressTrx != null && inProgressTrx.getContent() != null && !inProgressTrx.getContent().isEmpty()) {
          log.info("[TRANSACTION-IN-PROGRESS-CHECK] Found in-progress transactions for merchant {}", merchantId);
          return true;
        }
      } catch (Exception e) {
        log.error("[TRANSACTION-CHECK] Failed to query transactions for merchant {}: {}", merchantId, e.getMessage());
        throw e;
      }
    return false;
  }

  @Override
  public boolean hasProcessedTransactions(String merchantId, String initiativeId) {

      try {
        it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO processedTrx = transactionConnector.getPointOfSaleTransactions(merchantId, initiativeId, null, null, PageRequest.of(0, 1));

        if (processedTrx != null && processedTrx.getContent() != null && !processedTrx.getContent().isEmpty()) {
          log.info("[TRANSACTION-PROCESSED-CHECK] Found processed transactions for merchant {}", merchantId);
          return true;
        }
      } catch (Exception e) {
        log.error("[TRANSACTION-CHECK] Failed to query transactions for merchant {}: {}", merchantId, e.getMessage());
        throw e;
      }
    return false;
  }
}
