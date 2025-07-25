package it.gov.pagopa.merchant.repository;

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
        Query query = Query.query(criteria).with(pageable);
        return mongoTemplate.find(query, PointOfSale.class);
    }

    @Override
    public Criteria getCriteria(String merchantId, String type, String city, String address, String contactName) {
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(Criteria.where(PointOfSale.Fields.merchantId).is(merchantId));

        if(StringUtils.isNotBlank(type)){
            criteriaList.add(Criteria.where(PointOfSale.Fields.type).is(type));
        }

        if(StringUtils.isNotBlank(city)){
            criteriaList.add(Criteria.where(PointOfSale.Fields.city).is(city));
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

        String[] parts = address.split(",");
        String addressPart = parts[0].trim();
        addressCriterias.add(Criteria.where(PointOfSale.Fields.address).regex(Pattern.quote(addressPart),"i"));

        if (parts.length > 1) {
            String streetNumber = parts[1].trim();
            if (StringUtils.isNotBlank(streetNumber)) {
                addressCriterias.add(Criteria.where(PointOfSale.Fields.streetNumber).regex(Pattern.quote(streetNumber), "i"));
            }
        }

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
