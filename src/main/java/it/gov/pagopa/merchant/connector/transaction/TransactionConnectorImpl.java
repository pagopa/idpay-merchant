package it.gov.pagopa.merchant.connector.transaction;

import feign.FeignException;


import it.gov.pagopa.merchant.connector.transaction.dto.PointOfSaleTransactionsListDTO;
import it.gov.pagopa.merchant.exception.custom.TransactionInvocationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TransactionConnectorImpl implements TransactionConnector {

  private final TransactionRestClient restClient;

  public TransactionConnectorImpl(TransactionRestClient restClient) {
    this.restClient = restClient;
  }


  @Override
  public PointOfSaleTransactionsListDTO getPointOfSaleTransactions(String merchantId,
      String initiativeId, String pointOfSaleId, String productGtin, String fiscalCode,
      String status, Pageable pageable) {

    try {
      return restClient.getPointOfSaleTransactions(merchantId,
          initiativeId, pointOfSaleId, productGtin, fiscalCode, status, pageable);
    } catch (FeignException e) {
      throw new TransactionInvocationException(
          "An error occurred in the microservice merchant", true, e);
    }
  }
}
