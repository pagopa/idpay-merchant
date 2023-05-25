package it.gov.pagopa.merchant.repository;

public interface MerchantFileRepositoryExtended {
    void setMerchantFileStatus(String initiativeId, String fileName, String status);
}
