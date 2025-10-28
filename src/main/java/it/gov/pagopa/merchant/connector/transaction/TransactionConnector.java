package it.gov.pagopa.merchant.connector.transaction;


import it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO;
import it.gov.pagopa.merchant.dto.transaction.RewardTransaction;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TransactionConnector {

  MerchantTransactionsListDTO getMerchantTransactions(String merchantId, String initiativeId,
      String fiscalCode, String status, Pageable pageable);

  RewardTransaction findAll(String idTrxIssuer, String userId, LocalDateTime trxDateStart, LocalDateTime trxDateEnd, Long amountCents, Pageable pageable);
}
