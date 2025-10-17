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
import it.gov.pagopa.merchant.connector.payment.PaymentConnectorImpl;
import it.gov.pagopa.merchant.connector.payment.PaymentRestClient;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.exception.custom.PaymentInvocationException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class PaymentConnectorTest {

  private PaymentRestClient restClientMock;
  private PaymentConnectorImpl paymentConnector;

  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String INITIATIVE_ID = "INITIATIVE_ID";

  @BeforeEach
  void setUp() {
    restClientMock = mock(PaymentRestClient.class);
    paymentConnector = new PaymentConnectorImpl(restClientMock);
  }

  @Test
  void getPointOfSaleTransactions_throwsPaymentInvocationException() {
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

    when(restClientMock.getMerchantTransactions(
        MERCHANT_ID, INITIATIVE_ID,
        null, null, PageRequest.of(0, 1)))
        .thenThrow(exception);

    Runnable call = () -> paymentConnector.getMerchantTransactions(
        MERCHANT_ID, INITIATIVE_ID,
        null, null, PageRequest.of(0, 1)
    );

    assertThrows(PaymentInvocationException.class, call::run);
  }

  @Test
  void constructor_withMessage_setsGenericCode() {
    String message = "Generic error occurred";
    PaymentInvocationException ex = new PaymentInvocationException(message);

    assertNotNull(ex);
    assertEquals(MerchantConstants.ExceptionCode.GENERIC_ERROR, ex.getCode());
    assertEquals(message, ex.getMessage());
  }

  @Test
  void constructor_withCodeAndMessage_setsValues() {
    String code = "CUSTOM_CODE";
    String message = "Custom error occurred";
    PaymentInvocationException ex = new PaymentInvocationException(code, message);

    assertNotNull(ex);
    assertEquals(code, ex.getCode());
    assertEquals(message, ex.getMessage());
  }
}
