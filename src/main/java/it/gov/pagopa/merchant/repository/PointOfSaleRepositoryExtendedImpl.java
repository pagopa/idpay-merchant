package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
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
        Criteria criteria = Criteria.where(PointOfSale.Fields.merchantId).is(merchantId);

        if (StringUtils.isNotBlank(type)) {
            criteria.and(PointOfSale.Fields.type).is(type);
        }

        if (StringUtils.isNotBlank(city)) {
            criteria.and(PointOfSale.Fields.city).is(city);
        }

        if (StringUtils.isNotBlank(address)) {
            if (StringUtils.startsWithIgnoreCase(address,"http")) {
                Pattern websitePattern = Pattern.compile(Pattern.quote(address.trim()), Pattern.CASE_INSENSITIVE);
                criteria.and(PointOfSale.Fields.website).regex(websitePattern);
            }
            else {
                String[] parts = address.split(",");
                String addressPart = parts[0].trim();
                criteria.and(PointOfSale.Fields.address).regex(Pattern.quote(addressPart), "i");

                if (parts.length > 1) {
                    String streetNumber = parts[1].trim();
                    if (StringUtils.isNotBlank(streetNumber)) {
                        criteria.and(PointOfSale.Fields.streetNumber).regex(Pattern.quote(streetNumber), "i");
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(contactName)) {
            String[] nameParts = contactName.trim().split("\\s+");
            List<Criteria> nameCriterias = new ArrayList<>();

            for (String part : nameParts) {
                Pattern pattern = Pattern.compile(Pattern.quote(part), Pattern.CASE_INSENSITIVE);
                nameCriterias.add(new Criteria().orOperator(
                        Criteria.where(PointOfSale.Fields.contactName).regex(pattern),
                        Criteria.where(PointOfSale.Fields.contactSurname).regex(pattern)
                ));
            }

            if (!nameCriterias.isEmpty()) {
                criteria.andOperator(nameCriterias.toArray(new Criteria[0]));
            }
        }

        return criteria;
    }

    @Override
    public long getCount(Criteria criteria) {
        return mongoTemplate.count(Query.query(criteria), PointOfSale.class);
    }

}
