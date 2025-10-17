package it.gov.pagopa.merchant.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import it.gov.pagopa.merchant.connector.transaction.TransactionConnectorImpl;
import it.gov.pagopa.merchant.connector.transaction.TransactionRestClient;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.exception.custom.TransactionInvocationException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class TransactionConnectorTest {

  private TransactionRestClient restClientMock;
  private TransactionConnectorImpl transactionConnector;

  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String INITIATIVE_ID = "INITIATIVE_ID";

  @BeforeEach
  void setUp() {
    restClientMock = mock(TransactionRestClient.class);
    transactionConnector = new TransactionConnectorImpl(restClientMock);
  }

  @Test
  void getPointOfSaleTransactions_throwsTransactionInvocationException() {
    Request.Body body = Request.Body.create(new byte[0], StandardCharsets.UTF_8);
    RequestTemplate requestTemplate = new RequestTemplate();

    Request request = Request.create(
        Request.HttpMethod.GET,
        "http://localhost/test",
        Collections.emptyMap(),
        body,
        requestTemplate
    );

    Response response = Response.builder()
        .status(500)
        .reason("Internal Server Error")
        .request(request)
        .build();

    FeignException exception = FeignException.errorStatus("test", response);

    when(restClientMock.getPointOfSaleTransactions(
        MERCHANT_ID, INITIATIVE_ID,
        null, null, PageRequest.of(0, 1)))
        .thenThrow(exception);

    Runnable call = () -> transactionConnector.getPointOfSaleTransactions(
        MERCHANT_ID, INITIATIVE_ID,
        null, null, PageRequest.of(0, 1)
    );

    assertThrows(TransactionInvocationException.class, call::run);
  }

  @Test
  void constructor_withMessage_setsGenericCode() {
    String message = "Generic error occurred";
    TransactionInvocationException ex = new TransactionInvocationException(message);

    assertNotNull(ex);
    assertEquals(MerchantConstants.ExceptionCode.GENERIC_ERROR, ex.getCode());
    assertEquals(message, ex.getMessage());
  }

  @Test
  void constructor_withCodeAndMessage_setsValues() {
    String code = "CUSTOM_CODE";
    String message = "Custom error occurred";
    TransactionInvocationException ex = new TransactionInvocationException(code, message);

    assertNotNull(ex);
    assertEquals(code, ex.getCode());
    assertEquals(message, ex.getMessage());
  }

}
