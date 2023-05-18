package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.Merchant;
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
    public List<Merchant> findByFilter(Criteria criteria, Pageable pageable) {
        return mongoTemplate.find(Query.query(criteria).with(this.getPageable(pageable)), Merchant.class);
    }

    @Override
    public Criteria getCriteria(String initiativeId, String fiscalCode) {
        Criteria criteria = Criteria.where(Merchant.Fields.initiativeList).is(initiativeId);
        if (fiscalCode != null) {
            criteria.and(Merchant.Fields.fiscalCode).is(fiscalCode);
        }
        return criteria;
    }

    @Override
    public long getCount(Criteria criteria) {
        return mongoTemplate.count(Query.query(criteria), Merchant.class);
    }

    private Pageable getPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 15, Sort.by("lastUpdate"));
        }
        return pageable;
    }
}
