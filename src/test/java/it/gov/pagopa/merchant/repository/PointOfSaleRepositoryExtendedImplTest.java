package it.gov.pagopa.merchant.repository;

import com.mongodb.client.result.UpdateResult;
import org.bson.BsonValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
  @MockBean MongoTemplate mongoTemplate;
  @MockBean
  Criteria criteria;


  @Test
  void findByFilter() {
    Criteria criteria = new Criteria();
    Pageable paging = PageRequest.of(0, 20);
    repositoryExtended.findByFilter(criteria, paging);
    verify(mongoTemplate, times(1)).find(Mockito.any(), Mockito.any());
  }

  @Test
  void getCount() {
    Criteria criteria = new Criteria();
    repositoryExtended.getCount(criteria);
    verify(mongoTemplate, times(1)).count(Mockito.any(),
        (Class<?>) Mockito.any());
  }

  @Test
  void getCriteria() {
    Criteria criteria = repositoryExtended.getCriteria("MERCHANT-ID","TYPE","CITY","ADDRESS","CONTANCT-NAME");
    assertEquals(5, criteria.getCriteriaObject().size());
  }


}
