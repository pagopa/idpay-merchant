package it.gov.pagopa.merchant.service.merchant;



public interface MerchantTransactionCheckService {

  boolean hasInProgressTransactions(String merchantId, String initiativeId);
  boolean hasProcessedTransactions(String merchantId, String initiativeId);
}
