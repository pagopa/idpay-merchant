package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.ReportedUser;
import it.gov.pagopa.merchant.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ReportedUserRepositoryExtendedImpl implements ReportedUserRepositoryExtended {

    private static final Logger log = LoggerFactory.getLogger(ReportedUserRepositoryExtendedImpl.class);

    private final MongoTemplate mongoTemplate;

    public ReportedUserRepositoryExtendedImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<ReportedUser> findByFilter(Criteria criteria, Pageable pageable) {
        log.info("[REPORTED_USER_FIND] - Start findByFilter criteriaPresent={}, pageable={}", criteria != null, pageable);
        Query q = new Query();
        if (criteria != null) q.addCriteria(criteria);
        if (pageable != null) q.with(Utilities.getPageable(pageable));

        List<ReportedUser> result = mongoTemplate.find(q, ReportedUser.class);
        log.info("[REPORTED_USER_FIND] - Found {} reported users (criteriaPresent={}, pageable={})",
                result.size(), criteria != null, pageable != null);
        return result;
    }

    @Override
    public Criteria getCriteria(String merchantId, String initiativeId, String userId) {
        log.info("[REPORTED_USER_CRITERIA] - Building criteria with merchantId={}, initiativeId={}, userId={}",
                merchantId, initiativeId, userId);

        List<Criteria> ands = new ArrayList<>();
        if (merchantId != null)   ands.add(Criteria.where(ReportedUser.Fields.merchantId).is(merchantId));
        if (initiativeId != null) ands.add(Criteria.where(ReportedUser.Fields.initiativeId).is(initiativeId));
        if (userId != null)       ands.add(Criteria.where(ReportedUser.Fields.userId).is(userId));
        Criteria criteria = ands.isEmpty() ? new Criteria() : new Criteria().andOperator(ands.toArray(new Criteria[0]));

        log.info("[REPORTED_USER_CRITERIA] - Built criteria with {} conditions", ands.size());
        return criteria;
    }

    @Override
    public long getCount(Criteria criteria) {
        log.info("[REPORTED_USER_COUNT] - Start count with criteriaPresent={}", criteria != null);
        Query q = new Query();
        if (criteria != null) q.addCriteria(criteria);

        long count = mongoTemplate.count(q, ReportedUser.class);
        log.info("[REPORTED_USER_COUNT] - Found {} reported users matching criteria", count);
        return count;
    }

    @Override
    public long deleteByUserId(String userId) {
        log.info("[REPORTED_USER_DELETE] - Start delete for userId={}", userId);
        if (userId == null || userId.isBlank()) {
            log.warn("[REPORTED_USER_DELETE] - Skipped delete, userId is null or blank");
            return 0L;
        }
        Query q = Query.query(Criteria.where(ReportedUser.Fields.userId).is(userId));
        long deleted = mongoTemplate.remove(q, ReportedUser.class).getDeletedCount();
        log.info("[REPORTED_USER_DELETE] - Deleted {} reported users for userId={}", deleted, userId);
        return deleted;
    }
}
