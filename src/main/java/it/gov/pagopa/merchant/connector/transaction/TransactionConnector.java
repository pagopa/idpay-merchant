package it.gov.pagopa.merchant.connector.transaction;

import it.gov.pagopa.merchant.connector.transaction.dto.PointOfSaleTransactionsListDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface TransactionConnector {

  Mono<PointOfSaleTransactionsListDTO> getPointOfSaleTransactions(String merchantId, String initiativeId, String pointOfSaleId,
      String fiscalCode, String status, String productGtin, Pageable pageable);
}
