package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.dto.initiative.InitiativeResponse;
import it.gov.pagopa.merchant.model.Initiative;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface InitiativeRepository extends MongoRepository<Initiative, String> {

    @Aggregation(pipeline = {
        "{ $match: { initiativeId: { $nin: ?0 } } }",
        "{ $project: { " +
                "initiativeId: 1, " +
                "initiativeName: 1, " +
                "onboardable: { $gt: [ { $size: { $setIntersection: [ '$atecoCodes', ?1 ] } }, 0 ] } " +
        "} }"
    })
    List<InitiativeResponse> findFilteredInitiatives(Set<String> existingIds, List<String> atecoCodes);
}