package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.Merchant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MerchantRepository extends MongoRepository<Merchant, String>,MerchantRepositoryExtended {
    List<Merchant> findByInitiativeId(String initiativeId);
    Optional<Merchant> findByFiscalCodeAndAcquirerId(String fiscalCode, String acquirerId);
}
