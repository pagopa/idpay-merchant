package it.gov.pagopa.merchant.service;

public interface PDVService {
    String encryptCF(String fiscalCode);
    String decryptCF(String userId);
}
