package it.gov.pagopa.merchant.repository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.utils.Utilities;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MerchantRepositoryExtendedImpl implements MerchantRepositoryExtended {
    private final MongoTemplate mongoTemplate;

    public MerchantRepositoryExtendedImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Merchant> findByFilter(Criteria criteria, Pageable pageable) {
        return mongoTemplate.find(Query.query(criteria).with(Utilities.getPageable(pageable)), Merchant.class);
    }

    @Override
    public Criteria getCriteria(String initiativeId, String organizationId, String fiscalCode) {
        Criteria criteriaInitiative = Criteria.where(Initiative.Fields.initiativeId).is(initiativeId)
                .and(Initiative.Fields.organizationId).is(organizationId);
        Criteria criteria = Criteria.where(Merchant.Fields.initiativeList).elemMatch(criteriaInitiative);
        if (fiscalCode != null) {
            criteria.and(Merchant.Fields.fiscalCode).is(fiscalCode);
        }
        return criteria;
    }

    @Override
    public void updateInitiativeOnMerchant(String initiativeId) {
        Criteria criteriaInitiative = Criteria.where(Initiative.Fields.initiativeId).is(initiativeId);
        Criteria criteria = Criteria.where(Merchant.Fields.initiativeList).elemMatch(criteriaInitiative);
        mongoTemplate.updateMulti(Query.query(criteria),
            new Update().set("%s.$.%s".formatted(Merchant.Fields.initiativeList, Initiative.Fields.status), MerchantConstants.INITIATIVE_PUBLISHED)
                        .set("%s.$.%s".formatted(Merchant.Fields.initiativeList, Initiative.Fields.updateDate), LocalDateTime.now()),
            Merchant.class);
    }

    @Override
    public long getCount(Criteria criteria) {
        return mongoTemplate.count(Query.query(criteria), Merchant.class);
    }

    @Override
    public List<Merchant> findByInitiativeIdPageable(String initiativeId, int batchSize) {
        Criteria criteriaInitiative = Criteria.where(Initiative.Fields.initiativeId).is(initiativeId);
        Criteria criteria = Criteria.where(Merchant.Fields.initiativeList).elemMatch(criteriaInitiative);
        Pageable pageable = PageRequest.of(0, batchSize);
        Query query = Query.query(criteria).with(Utilities.getPageable(pageable));
        return mongoTemplate.find(query, Merchant.class);
    }

    @Override
    public UpdateResult findAndRemoveInitiativeOnMerchant(String initiativeId, String merchantId) {
        Criteria criteria = Criteria.where(Merchant.Fields.merchantId).is(merchantId);
        return mongoTemplate.updateFirst(Query.query(criteria),
                new Update().pull(Merchant.Fields.initiativeList, new BasicDBObject(Initiative.Fields.initiativeId,initiativeId)),
                Merchant.class);
    }

    @Override
    public Criteria getCriteria(String initiativeId) {
        Criteria criteriaInitiative = Criteria.where(Initiative.Fields.initiativeId).is(initiativeId);
        return Criteria.where(Merchant.Fields.initiativeList).elemMatch(criteriaInitiative);
    }

}
