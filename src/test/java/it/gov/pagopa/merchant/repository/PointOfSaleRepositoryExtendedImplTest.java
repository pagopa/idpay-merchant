package it.gov.pagopa.merchant.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.merchant.model.PointOfSale;
import org.bson.BsonValue;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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


  @AfterEach
  void mockitoVerify(){
    verifyNoMoreInteractions(mongoTemplate);
  }

  @Test
  void findByFilter() {
    Criteria criteria = new Criteria();
    Pageable paging = PageRequest.of(0, 20);

    when(mongoTemplate.find(any(Query.class), eq(PointOfSale.class)))
            .thenReturn(List.of());

    List<PointOfSale> result = repositoryExtended.findByFilter(criteria, paging);

    assertNotNull(result);
    verify(mongoTemplate, times(1)).find(any(Query.class), eq(PointOfSale.class));
  }

  @Test
  void findByFilter_sortByType() {
    Criteria criteria = new Criteria();
    Pageable paging = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "type"));
    AggregationResults<PointOfSale> aggregationResults = mock(AggregationResults.class);
    when(aggregationResults.getMappedResults()).thenReturn(List.of());

    when(mongoTemplate.aggregate(
            any(Aggregation.class),
            eq(PointOfSale.class),
            eq(PointOfSale.class)))
            .thenReturn(aggregationResults);

    List<PointOfSale> result = repositoryExtended.findByFilter(criteria, paging);

    assertNotNull(result);

    ArgumentCaptor<Aggregation> aggCaptor = ArgumentCaptor.forClass(Aggregation.class);
    verify(mongoTemplate, times(1)).aggregate(aggCaptor.capture(), eq(PointOfSale.class), eq(PointOfSale.class));

    Aggregation capturedAggregation = aggCaptor.getValue();
    Document aggDoc = capturedAggregation.toDocument("pointOfSale", Aggregation.DEFAULT_CONTEXT);
    List<Document> pipeline = aggDoc.getList("pipeline", Document.class);

    assertEquals(5, pipeline.size(), "La pipeline di aggregazione deve contenere esattamente 5 stadi");
    assertTrue(pipeline.get(0).containsKey("$match"));
    assertTrue(pipeline.get(1).containsKey("$addFields"));
    assertTrue(pipeline.get(2).containsKey("$sort"));
    assertTrue(pipeline.get(3).containsKey("$skip"));
    assertTrue(pipeline.get(4).containsKey("$limit"));
  }

  @Test
  void getCount() {
    Criteria criteria = new Criteria();
    repositoryExtended.getCount(criteria);
    verify(mongoTemplate, times(1)).count(Mockito.any(),
            (Class<?>) Mockito.any());
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
  void getCriteria_withPointOfSaleIds() {
    Criteria criteria = repositoryExtended.getCriteria(
        "MERCHANT-ID", List.of("POS-1", "POS-2"), null, null, null, null);

    String criteriaJson = criteria.getCriteriaObject().toJson();
    org.junit.jupiter.api.Assertions.assertTrue(criteriaJson.contains("_id"));
    org.junit.jupiter.api.Assertions.assertTrue(criteriaJson.contains("POS-1"));
    org.junit.jupiter.api.Assertions.assertTrue(criteriaJson.contains("POS-2"));
  }

  @Test
  void getCriteria_withEmptyPointOfSaleIds_doesNotAddIdFilter() {
    Criteria criteria = repositoryExtended.getCriteria(
        "MERCHANT-ID", List.of(), null, null, null, null);

    org.junit.jupiter.api.Assertions.assertFalse(
        criteria.getCriteriaObject().toJson().contains("_id"));
  }

  @Test
  void findDuplicate_onlineType_shouldQueryByWebsite_andReturnResult() {
    PointOfSale pos = PointOfSale.builder()
            .merchantId("MERCHANT-ID")
            .franchiseName("FRANCHISE-NAME")
            .type("ONLINE")
            .website("https://example.com")
            .build();

    PointOfSale expected = PointOfSale.builder()
            .id("pos1")
            .merchantId("MERCHANT-ID")
            .franchiseName("FRANCHISE-NAME")
            .type("ONLINE")
            .website("https://example.com")
            .build();

    when(mongoTemplate.findOne(any(Query.class), eq(PointOfSale.class)))
            .thenReturn(expected);

    Optional<PointOfSale> result = repositoryExtended.findDuplicate(pos);

    assertThat(result).isPresent();
    assertEquals("pos1", result.get().getId());

    Query query = captureQuery().getValue();
    Document q = query.getQueryObject();

    assertEquals("https://example.com", q.get(PointOfSale.Fields.website));
    assertFalse(q.containsKey(PointOfSale.Fields.address));
  }

  @Test
  void findDuplicate_onlineType_shouldNotQueryByAddress() {
    PointOfSale pos = PointOfSale.builder()
            .merchantId("MERCHANT-ID")
            .franchiseName("FRANCHISE-NAME")
            .type("ONLINE")
            .website("https://example.com")
            .address("Via Roma 1")
            .city("Milan")
            .build();

    when(mongoTemplate.findOne(any(Query.class), eq(PointOfSale.class)))
            .thenReturn(null);

    repositoryExtended.findDuplicate(pos);

    Query query = captureQuery().getValue();
    Document q = query.getQueryObject();

    assertFalse(q.containsKey(PointOfSale.Fields.address));
    assertFalse(q.containsKey(PointOfSale.Fields.city));
    assertTrue(q.containsKey(PointOfSale.Fields.website));
  }

  @Test
  void findDuplicate_physicalType_shouldQueryByAddress_andReturnResult() {
    PointOfSale pos = PointOfSale.builder()
            .merchantId("MERCHANT-ID")
            .franchiseName("FRANCHISE-NAME")
            .type("PHYSICAL")
            .address("Via Roma")
            .streetNumber("1")
            .city("Milan")
            .build();

    PointOfSale expected = PointOfSale.builder()
            .id("pos2")
            .merchantId("MERCHANT-ID")
            .franchiseName("FRANCHISE-NAME")
            .type("PHYSICAL")
            .address("Via Roma")
            .streetNumber("1")
            .city("Milan")
            .build();

    when(mongoTemplate.findOne(any(Query.class), eq(PointOfSale.class)))
            .thenReturn(expected);

    Optional<PointOfSale> result = repositoryExtended.findDuplicate(pos);

    assertThat(result).isPresent();
    assertEquals("pos2", result.get().getId());

    Query query = captureQuery().getValue();
    Document q = query.getQueryObject();

    assertEquals("Via Roma", q.get(PointOfSale.Fields.address));
    assertEquals("1", q.get(PointOfSale.Fields.streetNumber));
    assertEquals("Milan", q.get(PointOfSale.Fields.city));
    assertFalse(q.containsKey(PointOfSale.Fields.website));
  }

  @Test
  void findDuplicate_shouldAlwaysFilterByFranchiseNameAndType() {
    PointOfSale pos = PointOfSale.builder()
            .merchantId("MERCHANT-ID")
            .franchiseName("FRANCHISE-NAME")
            .type("PHYSICAL")
            .address("Via Roma")
            .streetNumber("1")
            .city("Milan")
            .build();

    when(mongoTemplate.findOne(any(Query.class), eq(PointOfSale.class)))
            .thenReturn(null);

    repositoryExtended.findDuplicate(pos);

    Query query = captureQuery().getValue();
    Document q = query.getQueryObject();

    assertEquals("FRANCHISE-NAME", q.get(PointOfSale.Fields.franchiseName));
    assertFalse(q.containsKey(PointOfSale.Fields.merchantId));
    assertEquals("PHYSICAL", q.get(PointOfSale.Fields.type));
  }

  private ArgumentCaptor<Query> captureQuery() {
    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(mongoTemplate).findOne(captor.capture(), eq(PointOfSale.class));
    return captor;
  }
}
