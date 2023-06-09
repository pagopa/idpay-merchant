package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.Merchant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public interface MerchantRepositoryExtended {
    List<Merchant> findByFilter(Criteria criteria, Pageable pageable);
    Criteria getCriteria(String initiativeId, String organizationId, String fiscalCode);
    long getCount(Criteria criteria);
}
