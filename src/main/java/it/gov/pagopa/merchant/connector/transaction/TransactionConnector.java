package it.gov.pagopa.merchant.connector.transaction;


import it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO;
import org.springframework.data.domain.Pageable;

public interface TransactionConnector {

  MerchantTransactionsListDTO getPointOfSaleTransactions(String merchantId, String initiativeId,
      String fiscalCode, String status, Pageable pageable);
}
