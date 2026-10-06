package it.gov.pagopa.merchant.configuration.pdnd;


import it.gov.pagopa.merchant.connector.pdnd.rest.PdndRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableFeignClients(clients = PdndRestClient.class)
public class PdndClientConfig { }