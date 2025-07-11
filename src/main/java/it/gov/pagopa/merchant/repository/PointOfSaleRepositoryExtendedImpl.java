package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.utils.Utilities;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PointOfSaleRepositoryExtendedImpl implements PointOfSaleRepositoryExtended {

    private final MongoTemplate mongoTemplate;

    public PointOfSaleRepositoryExtendedImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public List<PointOfSale> findByFilter(Criteria criteria, Pageable pageable) {
        return mongoTemplate.find(Query.query(criteria).with(Utilities.getPageable(pageable)), PointOfSale.class);
    }

    @Override
    public Criteria getCriteria(String merchantId, String type, String city, String address, String contactName) {

        Criteria criteria = Criteria.where(PointOfSale.Fields.merchantId).is(merchantId);

        if (type != null) {
            criteria.and(PointOfSale.Fields.type).is(type);
        }
        if (city != null) {
            criteria.and(PointOfSale.Fields.city).is(city);
        }
        if (address != null) {
            criteria.and(PointOfSale.Fields.address).is(address);
        }
        if (contactName != null) {
            criteria.and(PointOfSale.Fields.contactName).is(contactName);
        }
        return criteria;
        Query query = Query.query(criteria).with(Utilities.getPageable(pageable));
        return mongoTemplate.find(query, PointOfSale.class);
    }

    @Override
    public long getCount(Criteria criteria) {
        return mongoTemplate.count(Query.query(criteria), PointOfSale.class);
    }

}
