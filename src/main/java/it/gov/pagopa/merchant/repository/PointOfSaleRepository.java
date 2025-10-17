package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSale;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointOfSaleRepository extends MongoRepository<PointOfSale, String>, PointOfSaleRepositoryExtended {

    Optional<PointOfSale> findByIdAndMerchantId(String id, String merchantId);
    List<PointOfSale> findByMerchantId(String merchantId);
    void deleteByMerchantId(String merchantId);
    Optional<PointOfSale> findByContactEmail(String contactEmail);
}
