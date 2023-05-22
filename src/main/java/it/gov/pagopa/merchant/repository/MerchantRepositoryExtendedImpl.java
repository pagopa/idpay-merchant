package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.utils.Utilities;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MerchantRepositoryExtendedImpl implements MerchantRepositoryExtended {
    private final MongoTemplate mongoTemplate;
    private final Utilities utilities;

    public MerchantRepositoryExtendedImpl(MongoTemplate mongoTemplate, Utilities utilities) {
        this.mongoTemplate = mongoTemplate;
        this.utilities = utilities;
    }

    @Override
    public List<Merchant> findByFilter(Criteria criteria, Pageable pageable) {
        return mongoTemplate.find(Query.query(criteria).with(utilities.getPageable(pageable)), Merchant.class);
    }

    @Override
    public Criteria getCriteria(String initiativeId, String fiscalCode) {
        Criteria criteriaInitiative = Criteria.where(Initiative.Fields.initiativeId).is(initiativeId);
        Criteria criteria = Criteria.where(Merchant.Fields.initiativeList).elemMatch(criteriaInitiative);
        if (fiscalCode != null) {
            criteria.and(Merchant.Fields.fiscalCode).is(fiscalCode);
        }
        return criteria;
    }

    @Override
    public long getCount(Criteria criteria) {
        return mongoTemplate.count(Query.query(criteria), Merchant.class);
    }

}
