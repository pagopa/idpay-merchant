package it.gov.pagopa.merchant.connector.payment;

import feign.FeignException;
import it.gov.pagopa.merchant.connector.payment.dto.PointOfSaleTransactionsListDTO;
import it.gov.pagopa.merchant.exception.custom.PaymentInvocationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PaymentConnectorImpl implements PaymentConnector {

  private final PaymentRestClient restClient;

  public PaymentConnectorImpl(PaymentRestClient restClient) {
    this.restClient = restClient;
  }


  @Override
  public PointOfSaleTransactionsListDTO getPointOfSaleTransactions(String merchantId,
      String initiativeId, String pointOfSaleId, String fiscalCode, String status,
      String productGtin, Pageable pageable) {

    try {
     return restClient.getPointOfSaleTransactions(merchantId,
          initiativeId, pointOfSaleId, fiscalCode, status, productGtin, pageable);
    } catch (FeignException e) {
      throw new PaymentInvocationException(
          "An error occurred in the microservice merchant", true, e);
    }
  }
}
