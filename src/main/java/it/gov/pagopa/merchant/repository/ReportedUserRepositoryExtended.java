package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.ReportedUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public interface ReportedUserRepositoryExtended {

    List<ReportedUser> findByFilter(Criteria criteria, Pageable pageable);

    Criteria getCriteria(String merchantId, String initiativeId, String userId);

    long getCount(Criteria criteria);

    long deleteByUserId(String userId);

}
