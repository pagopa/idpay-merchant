package it.gov.pagopa.merchant.connector.payment;

import it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO;
import org.springframework.data.domain.Pageable;

public interface PaymentConnector {

  MerchantTransactionsListDTO getPointOfSaleTransactions(String merchantId, String initiativeId,
      String fiscalCode, String status, Pageable pageable);
}
