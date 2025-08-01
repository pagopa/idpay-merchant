package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointOfSaleRepository extends MongoRepository<PointOfSale, String>, PointOfSaleRepositoryExtended {

    Optional<PointOfSale> findByIdAndMerchantId(ObjectId id, String merchantId);


}
