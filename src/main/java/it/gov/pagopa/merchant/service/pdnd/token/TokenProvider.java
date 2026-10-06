package it.gov.pagopa.merchant.service.pdnd.token;


import it.gov.pagopa.merchant.dto.pdnd.ClientCredentialsResponse;
import it.gov.pagopa.merchant.dto.pdnd.PdndSecretValue;

public interface TokenProvider {
    ClientCredentialsResponse getTokenPdnd(PdndSecretValue pdndSecretValue);
}
