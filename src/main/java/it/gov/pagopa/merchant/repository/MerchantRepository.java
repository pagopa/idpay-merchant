package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends MongoRepository<Merchant, String>, MerchantRepositoryExtended {
    @Query(
            value = "{'initiativeList.initiativeId' : ?0, 'initiativeList.organizationId' : ?1, merchantId : ?2}",
            fields = "{merchantId : 1, " +
                    "businessName : 1, " +
                    "legalOfficeAddress : 1, " +
                    "legalOfficeMunicipality : 1, " +
                    "legalOfficeProvince : 1, " +
                    "legalOfficeZipCode : 1, " +
                    "certifiedEmail : 1, " +
                    "fiscalCode : 1, " +
                    "vatNumber : 1, " +
                    "iban : 1, " +
                    "'initiativeList.initiativeId' : 1, " +
                    "'initiativeList.initiativeName' : 1, " +
                    "'initiativeList.merchantStatus' : 1, " +
                    "'initiativeList.updateDate' : 1 }"

    )
    Optional<Merchant> retrieveByInitiativeIdAndOrganizationIdAndMerchantId(String initiativeId, String organizationId, String merchantId);

    @Query(
            value = "{'acquirerId' : ?0, 'fiscalCode' : ?1}",
            fields = "{merchantId : 1}"
    )
    Optional<Merchant> retrieveByAcquirerIdAndFiscalCode(String acquirerId, String fiscalCode);
    Optional<Merchant> findByFiscalCodeAndAcquirerId(String fiscalCode, String acquirerId);

    @Query(value = "{merchantId : ?0, 'initiativeList.initiativeId' : ?1}")
    Optional<Merchant> retrieveByMerchantIdAndInitiativeId(String merchantId, String initiativeId);

    @Query(value = "{'initiativeList.initiativeId' : ?1}")
    List<Initiative> deleteByInitiativeId(String initiativeId);
}
