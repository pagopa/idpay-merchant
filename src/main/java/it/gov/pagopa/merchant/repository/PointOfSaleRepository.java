package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointOfSaleRepository extends MongoRepository<PointOfSale, String>, PointOfSaleRepositoryExtended{
}
