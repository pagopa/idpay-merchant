package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.MerchantInitiative;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class MerchantRepositoryExtendedImpl implements MerchantRepositoryExtended {
    private final MongoTemplate mongoTemplate;

    public MerchantRepositoryExtendedImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<MerchantInitiative> findByFilter(Criteria criteria, Pageable pageable) {
        return mongoTemplate.find(Query.query(criteria).with(this.getPageable(pageable)), MerchantInitiative.class);
    }

    @Override
    public Criteria getCriteria(String initiativeId, String fiscalCode) {
        Criteria criteria = Criteria.where(MerchantInitiative.Fields.initiativeId).is(initiativeId);
        if (fiscalCode != null) {
            criteria.and(MerchantInitiative.Fields.fiscalCode).is(fiscalCode);
        }
        return criteria;
    }

    @Override
    public long getCount(Criteria criteria) {
        return mongoTemplate.count(Query.query(criteria), MerchantInitiative.class);
    }

    private Pageable getPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 15, Sort.by("lastUpdate"));
        }
        return pageable;
    }
}
