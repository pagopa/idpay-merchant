package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.MerchantInitiative;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MerchantRepository extends MongoRepository<MerchantInitiative, String>,MerchantRepositoryExtended {
    List<MerchantInitiative> findByInitiativeId(String initiativeId);
}
