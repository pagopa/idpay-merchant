package it.gov.pagopa.merchant.repository;


import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.model.MerchantFile.Fields;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

public class MerchantFileRepositoryExtendedImpl implements MerchantFileRepositoryExtended {
    private final MongoTemplate mongoTemplate;

    public MerchantFileRepositoryExtendedImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void setMerchantFileStatus(String initiativeId, String fileName, String status) {
        Query query = new Query(Criteria.where(Fields.initiativeId).is(initiativeId).and(Fields.fileName).is(fileName));
        Update update = new Update().set(Fields.status, status);
        mongoTemplate.updateFirst(query, update, MerchantFile.class);
    }
}
