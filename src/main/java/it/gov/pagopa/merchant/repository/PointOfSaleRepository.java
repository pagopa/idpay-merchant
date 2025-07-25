package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointOfSaleRepository extends MongoRepository<PointOfSale, String>, PointOfSaleRepositoryExtended {

    List<PointOfSale> findByContactEmail(String contactEmail);

}
