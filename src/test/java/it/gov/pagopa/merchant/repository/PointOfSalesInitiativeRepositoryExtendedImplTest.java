package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import java.time.Instant;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointOfSalesInitiativeRepositoryExtendedImplTest {

  @Mock
  private MongoTemplate mongoTemplate;

  private PointOfSalesInitiativeRepositoryExtendedImpl repository;

  @BeforeEach
  void setUp() {
    repository = new PointOfSalesInitiativeRepositoryExtendedImpl(mongoTemplate);
  }

  @Test
  void findPointOfSaleIdsByInitiativeIdAndMerchantIdAndEnabledTrue_returnsOnlyIds() {
    PointOfSalesInitiative relation1 = PointOfSalesInitiative.builder()
        .pointOfSaleId("POS_ID_1")
        .build();
    PointOfSalesInitiative relation2 = PointOfSalesInitiative.builder()
        .pointOfSaleId("POS_ID_2")
        .build();

    when(mongoTemplate.find(org.mockito.ArgumentMatchers.any(Query.class),
        org.mockito.ArgumentMatchers.eq(PointOfSalesInitiative.class)))
        .thenReturn(List.of(relation1, relation2));

    List<String> result = repository.findPointOfSaleIdsByInitiativeIdAndMerchantIdAndEnabledTrue(
        "INITIATIVE_ID", "MERCHANT_ID");

    Assertions.assertEquals(List.of("POS_ID_1", "POS_ID_2"), result);

    ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
    verify(mongoTemplate).find(queryCaptor.capture(),
        org.mockito.ArgumentMatchers.eq(PointOfSalesInitiative.class));

    Document queryObject = queryCaptor.getValue().getQueryObject();
    Assertions.assertEquals("INITIATIVE_ID", queryObject.get("initiativeId"));
    Assertions.assertEquals("MERCHANT_ID", queryObject.get("merchantId"));
    Assertions.assertEquals(true, queryObject.get("enabled"));

    Document fieldsObject = queryCaptor.getValue().getFieldsObject();
    Assertions.assertEquals(1, fieldsObject.get("pointOfSaleId"));
    Assertions.assertEquals(0, fieldsObject.get("_id"));
  }

  @Test
  void findInitiativesByPointOfSaleIdAndMerchantIdAndEnabledTrue_returnsInitiativeIdsAndCreatedAt() {
    Instant createdAt = Instant.parse("2026-06-26T10:15:30Z");
    Instant updatedAt = Instant.parse("2026-06-26T11:15:30Z");
    PointOfSalesInitiative relation = PointOfSalesInitiative.builder()
        .initiativeId("INITIATIVE_ID")
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();

    when(mongoTemplate.find(org.mockito.ArgumentMatchers.any(Query.class),
        org.mockito.ArgumentMatchers.eq(PointOfSalesInitiative.class)))
        .thenReturn(List.of(relation));

    List<PointOfSalesInitiative> result = repository
        .findInitiativesByPointOfSaleIdAndMerchantIdAndEnabledTrue("POS_ID", "MERCHANT_ID");

    Assertions.assertEquals(List.of(relation), result);

    ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
    verify(mongoTemplate).find(queryCaptor.capture(),
        org.mockito.ArgumentMatchers.eq(PointOfSalesInitiative.class));

    Document queryObject = queryCaptor.getValue().getQueryObject();
    Assertions.assertEquals("POS_ID", queryObject.get("pointOfSaleId"));
    Assertions.assertEquals("MERCHANT_ID", queryObject.get("merchantId"));
    Assertions.assertEquals(true, queryObject.get("enabled"));

    Document fieldsObject = queryCaptor.getValue().getFieldsObject();
    Assertions.assertEquals(1, fieldsObject.get("initiativeId"));
    Assertions.assertEquals(1, fieldsObject.get("createdAt"));
    Assertions.assertEquals(1, fieldsObject.get("updatedAt"));
    Assertions.assertEquals(0, fieldsObject.get("_id"));
  }
}
