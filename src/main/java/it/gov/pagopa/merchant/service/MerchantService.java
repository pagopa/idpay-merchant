package it.gov.pagopa.merchant.service;

public interface MerchantService {
    String retrieveMerchantId(String fiscalCode, String acquirerId);
}
