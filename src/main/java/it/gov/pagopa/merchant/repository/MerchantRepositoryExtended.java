package it.gov.pagopa.merchant.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.merchant.model.Merchant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public interface MerchantRepositoryExtended {
    List<Merchant> findByFilter(Criteria criteria, Pageable pageable);
    Criteria getCriteria(String initiativeId, String organizationId, String fiscalCode);
    UpdateResult findAndRemoveInitiativeOnMerchant (String initiativeId);
    void updateInitiativeOnMerchant(String initiativeId);
    long getCount(Criteria criteria);
}
