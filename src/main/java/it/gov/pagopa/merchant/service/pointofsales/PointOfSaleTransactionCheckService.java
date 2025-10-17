package it.gov.pagopa.merchant.service.pointofsales;



public interface PointOfSaleTransactionCheckService {

  boolean hasInProgressTransactions(String merchantId, String initiativeId);
  boolean hasProcessedTransactions(String merchantId, String initiativeId);
}
