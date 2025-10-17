package it.gov.pagopa.merchant.connector.transaction;

import feign.FeignException;

import it.gov.pagopa.merchant.connector.transaction.dto.MerchantTransactionsListDTO;
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
  public MerchantTransactionsListDTO getPointOfSaleTransactions(String merchantId,
      String initiativeId, String fiscalCode,
      String status, Pageable pageable) {

    try {
      return restClient.getPointOfSaleTransactions(merchantId,
          initiativeId, fiscalCode, status, pageable);
    } catch (FeignException e) {
      throw new TransactionInvocationException(
          "An error occurred in the microservice merchant", true, e);
    }
  }
}
