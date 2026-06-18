package it.gov.pagopa.merchant.connector.pdnd.service;


import it.gov.pagopa.merchant.connector.pdnd.dto.ClientCredentialsResponse;
import it.gov.pagopa.merchant.connector.pdnd.dto.PdndSecretValue;

public interface TokenProvider {
    ClientCredentialsResponse getTokenPdnd(PdndSecretValue pdndSecretValue);
}
