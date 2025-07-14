package it.gov.pagopa.merchant.repository;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.merchant.model.PointOfSale;
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
        Query query = Query.query(criteria).with(pageable);
        return mongoTemplate.find(query, PointOfSale.class);
    }

    @Override
    public Criteria getCriteria(String merchantId, String type, String city, String address, String contactName) {

        Criteria criteria = Criteria.where(PointOfSale.Fields.merchantId).is(merchantId);

        if (StringUtils.isNotBlank(type)) {
            criteria.and(PointOfSale.Fields.type).is(type);
        }
        if (StringUtils.isNotBlank(city)) {
            criteria.and(PointOfSale.Fields.city).is(city);
        }
        if (StringUtils.isNotBlank(address)) {
            criteria.and(PointOfSale.Fields.address).is(address);
        }
        if (StringUtils.isNotBlank(contactName)) {
            criteria.and(PointOfSale.Fields.contactName).is(contactName);
        }
        return criteria;
    }

    @Override
    public long getCount(Criteria criteria) {
        return mongoTemplate.count(Query.query(criteria), PointOfSale.class);
    }

}
