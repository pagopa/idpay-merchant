package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.ReportedUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReportedUserRepository
        extends MongoRepository<ReportedUser, String>, ReportedUserRepositoryExtended {

    boolean existsByUserId(String userId);

    boolean existsByUserIdAndInitiativeIdAndMerchantId(
            String userId,
            String initiativeId,
            String merchantId
    );


    List<ReportedUser> findByUserIdAndInitiativeIdAndMerchantId(
            String userId,
            String initiativeId,
            String merchantId
    );

    long deleteByUserIdAndInitiativeIdAndMerchantId(
            String userId,
            String initiativeId,
            String merchantId
    );
}
