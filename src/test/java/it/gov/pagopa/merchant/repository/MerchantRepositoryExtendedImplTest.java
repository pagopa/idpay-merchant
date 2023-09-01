package it.gov.pagopa.merchant.repository;

import com.mongodb.client.result.UpdateResult;
import org.bson.BsonValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = MerchantRepositoryExtendedImpl.class)
class MerchantRepositoryExtendedImplTest {
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
  MerchantRepositoryExtended merchantRepositoryExtended;
  @MockBean MongoTemplate mongoTemplate;
  @MockBean
  Criteria criteria;

  private static final String INITIATIVE_ID = "TEST_INITIATIVE_ID";
  private static final String ORGANIZATION_ID = "TEST_ORGANIZATION_ID";

  private static final String FISCAL_CODE = "FISCAL_CODE";

  @Test
  void findByFilter() {
    Criteria criteria = new Criteria();
    Pageable paging = PageRequest.of(0, 20, Sort.by("updateDate"));
    merchantRepositoryExtended.findByFilter(criteria, paging);
    verify(mongoTemplate, times(1)).find(Mockito.any(), Mockito.any());
  }
  @Test
  void getCount() {
    Criteria criteria = new Criteria();
    merchantRepositoryExtended.getCount(criteria);
    verify(mongoTemplate, times(1)).count(Mockito.any(),
        (Class<?>) Mockito.any());
  }

  @Test
  void getCriteria() {
    Criteria criteria = merchantRepositoryExtended.getCriteria(INITIATIVE_ID, ORGANIZATION_ID, FISCAL_CODE);
    assertEquals(2, criteria.getCriteriaObject().size());
  }

  @Test
  void getCriteriaWithoutOperationType() {
    Criteria criteria = merchantRepositoryExtended.getCriteria(INITIATIVE_ID, ORGANIZATION_ID,null);
    assertEquals(1, criteria.getCriteriaObject().size());
  }

  @Test
  void findAndRemoveInitiativeOnMerchant() {

    when(mongoTemplate.updateMulti(any(), any(), (Class<?>) any())).thenReturn(UPDATE_RESULT);

    UpdateResult result = merchantRepositoryExtended.findAndRemoveInitiativeOnMerchant(INITIATIVE_ID);

    Assertions.assertEquals(1, result.getModifiedCount());
  }

}
