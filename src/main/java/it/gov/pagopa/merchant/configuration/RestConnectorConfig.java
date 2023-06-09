package it.gov.pagopa.merchant.configuration;

import it.gov.pagopa.merchant.connector.initiative.InitiativeRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {
    InitiativeRestClient.class
})
public class RestConnectorConfig {

}
