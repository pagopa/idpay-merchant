package it.gov.pagopa.merchant.connector.transaction;

import it.gov.pagopa.merchant.connector.transaction.dto.PointOfSaleTransactionsListDTO;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
public class TransactionConnectorImpl implements TransactionConnector {

  private final WebClient webClient;
  private static final String URI_PROCESSED_TRANSACTIONS = "/idpay/initiatives/{initiativeId}/point-of-sales/{pointOfSaleId}/transactions/processed";
  private final int transactionsRetryDelay;
  private final long transactionsMaxAttempts;

  public TransactionConnectorImpl(
      @Qualifier("transactionWebClient") WebClient webClient,
      @Value("${app.transactions.retry.delay-millis}") int transactionsRetryDelay,
      @Value("${app.transactions.retry.max-attempts}") long transactionsMaxAttempts
  ) {
    this.webClient = webClient;
    this.transactionsRetryDelay = transactionsRetryDelay;
    this.transactionsMaxAttempts = transactionsMaxAttempts;
  }

  @Override
  public Mono<PointOfSaleTransactionsListDTO> getPointOfSaleTransactions(String merchantId,
      String initiativeId, String pointOfSaleId, String fiscalCode, String status,
      String productGtin, Pageable pageable) {

    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path(URI_PROCESSED_TRANSACTIONS)
            .queryParamIfPresent("fiscalCode", Optional.ofNullable(fiscalCode))
            .queryParamIfPresent("status", Optional.ofNullable(status))
            .queryParamIfPresent("productGtin", Optional.ofNullable(productGtin))
            .queryParam("page", pageable.getPageNumber())
            .queryParam("size", pageable.getPageSize())
            .queryParam("sort", String.join(",", pageable.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection())
                .toList()))
            .build(initiativeId, pointOfSaleId))
        .header("x-merchant-id", merchantId)
        .retrieve()
        .toEntity(PointOfSaleTransactionsListDTO.class)
        .map(HttpEntity::getBody)
        .retryWhen(
            Retry.fixedDelay(transactionsMaxAttempts, Duration.ofMillis(transactionsRetryDelay))
                .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests))
        .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
        .onErrorResume(WebClientResponseException.BadRequest.class, e -> Mono.empty());
  }
}
