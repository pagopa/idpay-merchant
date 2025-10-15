package it.gov.pagopa.merchant.connector.payment;

import it.gov.pagopa.merchant.connector.payment.dto.PointOfSaleTransactionsListDTO;
import org.springframework.data.domain.Pageable;

public interface PaymentConnector {

  PointOfSaleTransactionsListDTO getPointOfSaleTransactions(String merchantId, String initiativeId, String pointOfSaleId,
      String fiscalCode, String status, String productGtin, Pageable pageable);
}
