package it.gov.pagopa.merchant.connector.payment;

import feign.FeignException;
import it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO;
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
  public MerchantTransactionsListDTO getMerchantTransactions(String merchantId,
      String initiativeId, String fiscalCode, String status, Pageable pageable) {

    try {
     return restClient.getMerchantTransactions(merchantId,
          initiativeId, fiscalCode, status, pageable);
    } catch (FeignException e) {
      throw new PaymentInvocationException(
          "An error occurred in the microservice merchant", true, e);
    }
  }
}
