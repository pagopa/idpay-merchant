package it.gov.pagopa.merchant.repository;

import static it.gov.pagopa.merchant.constants.AggregationConstants.CASE_INSENSITIVE_FIELDS;
import static it.gov.pagopa.merchant.constants.AggregationConstants.CONTACT_SURNAME_LOWER_SUFFIX;

import it.gov.pagopa.merchant.constants.AggregationConstants;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class PointOfSaleRepositoryExtendedImpl implements PointOfSaleRepositoryExtended {

    private final MongoTemplate mongoTemplate;


    public PointOfSaleRepositoryExtendedImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public List<PointOfSale> findByFilter(Criteria criteria, Pageable pageable) {
        boolean hasCaseInsensitiveSort = pageable.getSort().stream()
            .anyMatch(o -> CASE_INSENSITIVE_FIELDS.contains(o.getProperty()));

        if (hasCaseInsensitiveSort) {
            Aggregation aggregation = buildCaseInsensitiveAggregation(criteria, pageable);
            return mongoTemplate.aggregate(aggregation, PointOfSale.class, PointOfSale.class)
                .getMappedResults();
        } else {
            Query query = Query.query(criteria).with(pageable);
            return mongoTemplate.find(query, PointOfSale.class);
        }
    }

    private Aggregation buildCaseInsensitiveAggregation(Criteria criteria, Pageable pageable) {
        List<AggregationOperation> ops = new ArrayList<>();

        boolean sortByContactName = false;

        for (Sort.Order order : pageable.getSort()) {
            if (CASE_INSENSITIVE_FIELDS.contains(order.getProperty())) {
                ops.add(Aggregation.addFields()
                    .addField(order.getProperty() + AggregationConstants.LOWER_SUFFIX)
                    .withValue(new org.bson.Document("$toLower", "$" + order.getProperty()))
                    .build());
            }

            if ("contactName".equalsIgnoreCase(order.getProperty())) {
                sortByContactName = true;
            }
        }

        if (sortByContactName) {
            ops.add(Aggregation.addFields()
                .addField("contactSurname" + AggregationConstants.LOWER_SUFFIX)
                .withValue(new org.bson.Document("$toLower", "$contactSurname"))
                .build());
        }

        ops.add(Aggregation.match(criteria));

        List<Sort.Order> sortOrders = new ArrayList<>();

        pageable.getSort().forEach(order -> {
            String prop = CASE_INSENSITIVE_FIELDS.contains(order.getProperty())
                ? order.getProperty() + AggregationConstants.LOWER_SUFFIX
                : order.getProperty();
            sortOrders.add(new Sort.Order(order.getDirection(), prop));

            if ("contactName".equalsIgnoreCase(order.getProperty())) {
                sortOrders.add(new Sort.Order(order.getDirection(), CONTACT_SURNAME_LOWER_SUFFIX));
            }
        });

        ops.add(Aggregation.sort(Sort.by(sortOrders)));
        ops.add(Aggregation.skip(pageable.getOffset()));
        ops.add(Aggregation.limit(pageable.getPageSize()));

        ProjectionOperation project = Aggregation.project(PointOfSale.class);

        for (Sort.Order order : pageable.getSort()) {
            if (CASE_INSENSITIVE_FIELDS.contains(order.getProperty())) {
                project = project.andExclude(
                    order.getProperty() + AggregationConstants.LOWER_SUFFIX);
            }
        }

        if (sortByContactName) {
            project = project.andExclude("contactSurname" + AggregationConstants.LOWER_SUFFIX);
        }

        ops.add(project);

        return Aggregation.newAggregation(ops);
    }

    @Override
    public Criteria getCriteria(String merchantId, String type, String city, String address, String contactName) {
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(Criteria.where(PointOfSale.Fields.merchantId).is(merchantId));

        if(StringUtils.isNotBlank(type)){
            Pattern typePattern = Pattern.compile(Pattern.quote(type.trim()), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where(PointOfSale.Fields.type).regex(typePattern));
        }

        if(StringUtils.isNotBlank(city)){
            Pattern cityPattern = Pattern.compile(Pattern.quote(city.trim()), Pattern.CASE_INSENSITIVE);
            criteriaList.add(Criteria.where(PointOfSale.Fields.city).regex(cityPattern));
        }

        if (StringUtils.isNotBlank(address)) {
            criteriaList.add(buildAddressCriteria(address));
        }

        if (StringUtils.isNotBlank(contactName)) {
            criteriaList.add(buildContactNameCriteria(contactName));
        }

        return new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
    }

    @Override
    public long getCount(Criteria criteria) {
        return mongoTemplate.count(Query.query(criteria), PointOfSale.class);
    }

    private Criteria buildAddressCriteria(String address){
        Pattern inputPattern = Pattern.compile(Pattern.quote(address.trim()), Pattern.CASE_INSENSITIVE);

        List<Criteria> addressCriterias = new ArrayList<>();

        addressCriterias.add(Criteria.where(PointOfSale.Fields.website).regex(inputPattern));

        Pattern addressPattern = Pattern.compile(Pattern.quote(address), Pattern.CASE_INSENSITIVE);
        addressCriterias.add(Criteria.where(PointOfSale.Fields.address).regex(addressPattern));

        return new Criteria().orOperator(addressCriterias.toArray(new Criteria[0]));
    }

    private Criteria buildContactNameCriteria(String contactName){
        String[] nameParts = contactName.trim().split("\\s+");
        List<Criteria> nameCriterias = new ArrayList<>();

        for (String part : nameParts) {
            Pattern pattern = Pattern.compile(Pattern.quote(part), Pattern.CASE_INSENSITIVE);
            nameCriterias.add(new Criteria().orOperator(
                    Criteria.where(PointOfSale.Fields.contactName).regex(pattern),
                    Criteria.where(PointOfSale.Fields.contactSurname).regex(pattern),
                    Criteria.where(PointOfSale.Fields.contactEmail).regex(pattern)
            ));
        }

        return new Criteria().andOperator(nameCriterias.toArray(new Criteria[0]));
    }

}

