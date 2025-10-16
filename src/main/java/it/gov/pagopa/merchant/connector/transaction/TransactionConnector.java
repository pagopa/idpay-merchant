package it.gov.pagopa.merchant.connector.transaction;


import it.gov.pagopa.merchant.connector.transaction.dto.PointOfSaleTransactionsListDTO;
import org.springframework.data.domain.Pageable;

public interface TransactionConnector {

  PointOfSaleTransactionsListDTO getPointOfSaleTransactions(String merchantId, String initiativeId, String pointOfSaleId,
      String fiscalCode, String status, String productGtin, Pageable pageable);
}
