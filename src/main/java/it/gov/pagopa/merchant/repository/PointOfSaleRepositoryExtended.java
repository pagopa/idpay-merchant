package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Optional;

public interface PointOfSaleRepositoryExtended {
    List<PointOfSale> findByFilter(Criteria criteria, Pageable pageable);
    Criteria getCriteria(String merchantId, String type, String city, String address, String contactName);
    long getCount(Criteria criteria);

    Optional<PointOfSale> findByMerchantIdAndObjectId(String merchantId, ObjectId pointOfSaleId);
}
