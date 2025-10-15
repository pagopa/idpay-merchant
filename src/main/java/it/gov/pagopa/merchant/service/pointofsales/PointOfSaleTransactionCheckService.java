package it.gov.pagopa.merchant.service.pointofsales;

import java.util.List;
import reactor.core.publisher.Mono;

public interface PointOfSaleTransactionCheckService {

  boolean hasInProgressTransactions(String merchantId, String initiativeId, List<String> posIds);
  Mono<Boolean> hasProcessedTransactions(String merchantId, String initiativeId, List<String> posIds);
}
