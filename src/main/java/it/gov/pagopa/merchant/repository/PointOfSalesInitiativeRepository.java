package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PointOfSalesInitiativeRepository
        extends MongoRepository<PointOfSalesInitiative, String>, PointOfSalesInitiativeRepositoryExtended {

    Optional<PointOfSalesInitiative> findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
            String pointOfSaleId, String initiativeId, String merchantId
    );
}
