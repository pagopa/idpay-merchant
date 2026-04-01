package it.gov.pagopa.merchant.connector.transaction;


import it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO;
import it.gov.pagopa.merchant.dto.transaction.RewardTransaction;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface TransactionConnector {

  MerchantTransactionsListDTO getMerchantTransactions(String merchantId, String initiativeId,
      String fiscalCode, String status, Pageable pageable);

  List<RewardTransaction> findAll(String idTrxIssuer, String userId, Instant trxDateStart, Instant trxDateEnd, Long amountCents, Pageable pageable);
}
