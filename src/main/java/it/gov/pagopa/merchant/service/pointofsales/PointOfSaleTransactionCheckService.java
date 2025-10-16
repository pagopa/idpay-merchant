package it.gov.pagopa.merchant.service.pointofsales;

import java.util.List;

public interface PointOfSaleTransactionCheckService {

  boolean hasInProgressTransactions(String merchantId, String initiativeId, List<String> posIds);
  boolean hasProcessedTransactions(String merchantId, String initiativeId, List<String> posIds);
}
