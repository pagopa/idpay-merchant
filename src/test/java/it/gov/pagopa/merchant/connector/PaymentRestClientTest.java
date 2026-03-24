package it.gov.pagopa.merchant.connector;

import com.github.tomakehurst.wiremock.WireMockServer;
import it.gov.pagopa.merchant.configuration.RestConnectorConfig;
import it.gov.pagopa.merchant.connector.payment.PaymentConnector;
import it.gov.pagopa.merchant.connector.payment.PaymentConnectorImpl;
import it.gov.pagopa.merchant.connector.payment.dto.MerchantTransactionsListDTO;
import it.gov.pagopa.merchant.exception.custom.PaymentInvocationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(
        classes = {
                PaymentConnectorImpl.class,
                RestConnectorConfig.class,
                FeignAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
                "spring.application.name=idpay-payment-integration-rest",
                "rest-client.payment.baseUrl=http://localhost:8089"
        })
class PaymentRestClientTest {

  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String INITIATIVE_ID = "INITIATIVE_ID";
  private static final String INITIATIVE_ID_ERROR = "INITIATIVE_ID_ERROR";

  private static WireMockServer wireMockServer;

  @Autowired
  private PaymentConnector restConnector;

  @BeforeAll
  static void startWireMock() {
    wireMockServer = new WireMockServer(8089);
    wireMockServer.start();
    configureFor("localhost", 8089);
  }

  @AfterAll
  static void stopWireMock() {
    wireMockServer.stop();
  }

  @BeforeEach
  void setupStubs() {

    // risposta OK
    stubFor(get(urlPathEqualTo("/idpay/payment/merchant/" + MERCHANT_ID + "/transactions"))
            .withQueryParam("initiativeId", equalTo(INITIATIVE_ID))
            .willReturn(okJson("""
                        {
                          "transactions": [],
                          "totalElements": 0,
                          "totalPages": 0
                        }
                        """)));

    // risposta errore server
    stubFor(get(urlPathEqualTo("/idpay/payment/merchant/" + MERCHANT_ID + "/transactions"))
            .withQueryParam("initiativeId", equalTo(INITIATIVE_ID_ERROR))
            .willReturn(aResponse()
                    .withStatus(500)));
  }

  @Test
  void getPointOfSaleTransactions() {
    Pageable pageable = PageRequest.of(0, 10);

    MerchantTransactionsListDTO actual = restConnector.getMerchantTransactions(
            MERCHANT_ID,
            INITIATIVE_ID,
            null,
            null,
            pageable
    );

    assertNotNull(actual);
  }

  @Test
  void getPointOfSaleTransactions_serverError() {
    Pageable pageable = PageRequest.of(0, 10);

    assertThrows(PaymentInvocationException.class, () ->
            restConnector.getMerchantTransactions(
                    MERCHANT_ID,
                    INITIATIVE_ID_ERROR,
                    null,
                    null,
                    pageable
            )
    );
  }
}