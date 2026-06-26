package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import java.util.List;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class PointOfSalesInitiativeRepositoryExtendedImpl implements PointOfSalesInitiativeRepositoryExtended {

    private final MongoTemplate mongoTemplate;

    public PointOfSalesInitiativeRepositoryExtendedImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<String> findPointOfSaleIdsByInitiativeIdAndMerchantIdAndEnabledTrue(
            String initiativeId, String merchantId) {
        Query query = Query.query(Criteria.where(PointOfSalesInitiative.Fields.initiativeId).is(initiativeId)
                .and(PointOfSalesInitiative.Fields.merchantId).is(merchantId)
                .and(PointOfSalesInitiative.Fields.enabled).is(true));
        query.fields()
                .include(PointOfSalesInitiative.Fields.pointOfSaleId)
                .exclude("_id");

        return mongoTemplate.find(query, PointOfSalesInitiative.class).stream()
                .map(PointOfSalesInitiative::getPointOfSaleId)
                .toList();
    }

    @Override
    public List<PointOfSalesInitiative> findInitiativesByPointOfSaleIdAndMerchantIdAndEnabledTrue(
            String pointOfSaleId, String merchantId) {
        Query query = Query.query(Criteria.where(PointOfSalesInitiative.Fields.pointOfSaleId).is(pointOfSaleId)
                .and(PointOfSalesInitiative.Fields.merchantId).is(merchantId)
                .and(PointOfSalesInitiative.Fields.enabled).is(true));
        query.fields()
                .include(PointOfSalesInitiative.Fields.initiativeId)
                .include(PointOfSalesInitiative.Fields.createdAt)
                .exclude("_id");

        return mongoTemplate.find(query, PointOfSalesInitiative.class);
    }
}
