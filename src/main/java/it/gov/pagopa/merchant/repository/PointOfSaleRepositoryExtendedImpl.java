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
        Query query = Query.query(criteria).with(Utilities.getPageable(pageable));
        return mongoTemplate.find(query, PointOfSale.class);
    }



}
