package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.MerchantInitiative;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public interface MerchantRepositoryExtended {
    List<MerchantInitiative> findByFilter(Criteria criteria, Pageable pageable);
    Criteria getCriteria(String initiativeId, String fiscalCode);
    long getCount(Criteria criteria);
}
