package it.gov.pagopa.merchant.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.merchant.model.PointOfSale;
import java.util.List;
import org.bson.BsonValue;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = PointOfSaleRepositoryExtendedImpl.class)
class PointOfSaleRepositoryExtendedImplTest {
  public static final UpdateResult UPDATE_RESULT = new UpdateResult() {
    @Override
    public boolean wasAcknowledged() {
      return false;
    }

    @Override
    public long getMatchedCount() {
      return 0;
    }

    @Override
    public long getModifiedCount() {
      return 1;
    }

    @Override
    public BsonValue getUpsertedId() {
      return null;
    }
  };
  @Autowired
  PointOfSaleRepositoryExtended repositoryExtended;
  @MockitoBean
  MongoTemplate mongoTemplate;


  @Test
  void findByFilter() {
    Criteria criteria = new Criteria();
    Pageable paging = PageRequest.of(0, 20);
    repositoryExtended.findByFilter(criteria, paging);
    verify(mongoTemplate, times(1)).find(any(), any());
  }

  @Test
  void getCount() {
    Criteria criteria = new Criteria();
    repositoryExtended.getCount(criteria);
    verify(mongoTemplate, times(1)).count(any(),
            (Class<?>) any());
  }

  @Test
  void getCriteria_withAddress() {
    Criteria criteria = repositoryExtended.getCriteria("MERCHANT-ID","TYPE","CITY","ADDRESS","CONTANCT-NAME");
    assertEquals(1, criteria.getCriteriaObject().size());
  }

  @Test
  void getCriteria_withAddressAndStreetNumber() {
    Criteria criteria = repositoryExtended.getCriteria("MERCHANT-ID","TYPE","CITY","ADDRESS, 22","CONTANCT-NAME");
    assertEquals(1, criteria.getCriteriaObject().size());
  }

  @Test
  void getCriteria_typeNullAndCityNull() {
    Criteria criteria = repositoryExtended.getCriteria("MERCHANT-ID",null,null,"https://google.com","CONTANCT-NAME");
    assertEquals(1, criteria.getCriteriaObject().size());
  }

  @Test
  void getCriteria_withAddressAndStreetNumberEmpty() {
    Criteria criteria = repositoryExtended.getCriteria("MERCHANT-ID","TYPE","CITY","ADDRESS, ","CONTANCT-NAME");
    assertEquals(1, criteria.getCriteriaObject().size());
  }

  @Test
  void getCriteria_withAddressNullAndContactNameNull() {
    Criteria criteria = repositoryExtended.getCriteria("MERCHANT-ID","TYPE","CITY",null,null);
    assertEquals(1, criteria.getCriteriaObject().size());
  }

  @Test
  void testFindByFilter_WithCaseInsensitiveSort() {
    Criteria criteria = new Criteria();
    Pageable pageable = PageRequest.of(0, 8, Sort.by("contactName").ascending());

    List<PointOfSale> dummyResult = List.of(new PointOfSale());
    AggregationResults<PointOfSale> aggregationResults = new AggregationResults<>(dummyResult, new Document());

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(PointOfSale.class), eq(PointOfSale.class)))
        .thenReturn(aggregationResults);

    List<PointOfSale> result = repositoryExtended.findByFilter(criteria, pageable);

    assertEquals(1, result.size());
    verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), eq(PointOfSale.class), eq(PointOfSale.class));
    verify(mongoTemplate, never()).find(any(Query.class), eq(PointOfSale.class));
  }

  @Test
  void testFindByFilter_WithoutCaseInsensitiveSort() {
    Criteria criteria = new Criteria();
    Pageable pageable = PageRequest.of(0, 8, Sort.by("city").ascending());

    List<PointOfSale> dummyResult = List.of(new PointOfSale());
    when(mongoTemplate.find(any(Query.class), eq(PointOfSale.class))).thenReturn(dummyResult);

    List<PointOfSale> result = repositoryExtended.findByFilter(criteria, pageable);

    assertEquals(1, result.size());
    verify(mongoTemplate, never()).aggregate(any(Aggregation.class), eq(PointOfSale.class), eq(PointOfSale.class));
    verify(mongoTemplate, times(1)).find(any(Query.class), eq(PointOfSale.class));
  }

  @Test
  void testFindByFilter_WithMixedSort() {
    Criteria criteria = new Criteria();
    Pageable pageable = PageRequest.of(0, 8, Sort.by(
        Sort.Order.asc("contactName"),
        Sort.Order.desc("city")
    ));

    List<PointOfSale> dummyResult = List.of(new PointOfSale());
    AggregationResults<PointOfSale> aggregationResults = new AggregationResults<>(dummyResult, new Document());

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(PointOfSale.class), eq(PointOfSale.class)))
        .thenReturn(aggregationResults);

    List<PointOfSale> result = repositoryExtended.findByFilter(criteria, pageable);

    assertEquals(1, result.size());
    verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), eq(PointOfSale.class), eq(PointOfSale.class));
    verify(mongoTemplate, never()).find(any(Query.class), eq(PointOfSale.class));
  }
}
