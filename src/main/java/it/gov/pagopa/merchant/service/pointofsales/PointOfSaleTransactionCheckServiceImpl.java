package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.connector.payment.PaymentConnector;
import it.gov.pagopa.merchant.connector.payment.dto.PointOfSaleTransactionsListDTO;
import it.gov.pagopa.merchant.connector.transaction.TransactionConnector;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
  public boolean hasInProgressTransactions(String merchantId, String initiativeId, List<String> posIds) {
    if (posIds == null || posIds.isEmpty()) {
      return false;
    }

    for (String posId : posIds) {
      try {
        PointOfSaleTransactionsListDTO inProgressTrx = paymentConnector.getPointOfSaleTransactions(merchantId, initiativeId, posId, null, null, null, PageRequest.of(0,1));

        if (inProgressTrx != null && inProgressTrx.getContent() != null && !inProgressTrx.getContent().isEmpty()) {
          log.info("[TRANSACTION-IN-PROGRESS-CHECK] Found in-progress transactions for POS {}", posId);
          return true;
        }
      } catch (Exception e) {
        log.error("[TRANSACTION-CHECK] Failed to query transactions for POS {}: {}", posId, e.getMessage());
        throw e;
      }
    }
    return false;
  }

  @Override
  public Mono<Boolean> hasProcessedTransactions(String merchantId, String initiativeId, List<String> posIds) {
    return Flux.fromIterable(posIds)
        .flatMap(posId -> transactionConnector
            .getPointOfSaleTransactions(merchantId, initiativeId, posId, null, null, null, PageRequest.of(0,1))
            .map(trx -> trx.getContent() != null && !trx.getContent().isEmpty())
            .onErrorReturn(false)
        )
        .any(Boolean::booleanValue);
  }
}
