package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public interface PointOfSaleRepositoryExtended {
    List<PointOfSale> findByFilter(Criteria criteria, Pageable pageable);
    long getCount(Criteria criteria);
}
