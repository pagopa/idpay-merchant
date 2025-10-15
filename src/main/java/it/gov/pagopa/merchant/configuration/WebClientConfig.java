package it.gov.pagopa.merchant.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient transactionWebClient(WebClient.Builder builder,
      @Value("${app.transactions.baseUrl}") String transactionBaseUrl) {
    return builder.baseUrl(transactionBaseUrl).build();
  }
}
