package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.MerchantFile;

import java.util.List;

public interface MerchantFileRepositoryExtended {
    void setMerchantFileStatus(String initiativeId, String fileName, String status);
    List<MerchantFile> deletePaged(String initiativeId, int pageSize);
}
