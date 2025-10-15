package it.gov.pagopa.merchant.configuration;

import it.gov.pagopa.merchant.connector.initiative.InitiativeRestClient;
import it.gov.pagopa.merchant.connector.payment.PaymentRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {
    InitiativeRestClient.class,
    PaymentRestClient.class
})
public class RestConnectorConfig {

}
