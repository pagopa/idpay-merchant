package it.gov.pagopa.merchant.connector;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import it.gov.pagopa.merchant.configuration.RestConnectorConfig;
import it.gov.pagopa.merchant.connector.payment.PaymentConnector;
import it.gov.pagopa.merchant.connector.payment.PaymentConnectorImpl;
import it.gov.pagopa.merchant.connector.payment.dto.PointOfSaleTransactionsListDTO;
import it.gov.pagopa.merchant.exception.custom.PaymentInvocationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(
    classes = {
        PaymentConnectorImpl.class,
        RestConnectorConfig.class,
        FeignAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class
    })
@AutoConfigureWireMock(stubs = "classpath:/mappings", port = 0)
@TestPropertySource(
    locations = "classpath:application.yml",
    properties = {
        "spring.application.name=idpay-payment-integration-rest",
        "rest-client.payment.baseUrl=http://localhost:${wiremock.server.port}",
    })
class PaymentRestClientTest {

  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String INITIATIVE_ID = "INITIATIVE_ID";
  private static final String POINT_OF_SALE_ID = "POS_ID";

  @Autowired
  private PaymentConnector restConnector;

  @Test
  void getPointOfSaleTransactions() {
    Pageable pageable = PageRequest.of(0, 10);
    PointOfSaleTransactionsListDTO actual = restConnector.getPointOfSaleTransactions(
        MERCHANT_ID,
        INITIATIVE_ID,
        POINT_OF_SALE_ID,
        null,
        null,
        null,
        pageable
    );

    assertNotNull(actual);
  }

  @Test
  void getPointOfSaleTransactions_serverError() {
    Pageable pageable = PageRequest.of(0, 10);

    assertThrows(PaymentInvocationException.class, () -> {
      restConnector.getPointOfSaleTransactions(
          MERCHANT_ID,
          "INITIATIVE_ID_ERROR",
          POINT_OF_SALE_ID,
          null,
          null,
          null,
          pageable
      );
    });
  }
}
