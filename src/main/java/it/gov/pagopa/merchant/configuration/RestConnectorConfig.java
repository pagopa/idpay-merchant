package it.gov.pagopa.merchant.configuration;

import it.gov.pagopa.merchant.connector.encrypt.EncryptRest;
import it.gov.pagopa.merchant.connector.initiative.InitiativeRestClient;
import it.gov.pagopa.merchant.connector.payment.PaymentRestClient;
import it.gov.pagopa.merchant.connector.transaction.TransactionRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {
        EncryptRest.class,
    InitiativeRestClient.class,
    PaymentRestClient.class,
    TransactionRestClient.class
})
public class RestConnectorConfig {

}
