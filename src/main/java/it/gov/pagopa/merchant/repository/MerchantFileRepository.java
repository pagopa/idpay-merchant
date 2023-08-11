package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.MerchantFile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MerchantFileRepository extends MongoRepository<MerchantFile, String>,MerchantFileRepositoryExtended {
    List<MerchantFile> findByFileNameAndInitiativeId(String fileName, String initiativeId);
    List<MerchantFile> deleteByInitiativeId(String initiativeId);
}
