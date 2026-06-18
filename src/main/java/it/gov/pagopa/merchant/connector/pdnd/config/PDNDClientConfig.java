package it.gov.pagopa.merchant.connector.pdnd.config;


import it.gov.pagopa.merchant.connector.pdnd.rest.PDNDRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableFeignClients(clients = PDNDRestClient.class)
public class PDNDClientConfig { }