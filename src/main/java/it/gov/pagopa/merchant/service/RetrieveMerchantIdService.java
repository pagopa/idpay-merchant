package it.gov.pagopa.merchant.service;

public interface RetrieveMerchantIdService {
    String getByFiscalCodeAndAcquirerId(String fiscalCode, String acquirerId);
}
