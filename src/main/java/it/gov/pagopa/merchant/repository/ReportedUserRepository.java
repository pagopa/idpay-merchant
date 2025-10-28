package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.ReportedUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReportedUserRepository
        extends MongoRepository<ReportedUser, String>, ReportedUserRepositoryExtended {

    boolean existsByUserId(String userId);
}
