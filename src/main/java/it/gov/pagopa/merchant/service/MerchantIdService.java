package it.gov.pagopa.merchant.service;

public interface MerchantIdService {
    String getMerchantInfo(String acquirerId, String fiscalCode);
}
