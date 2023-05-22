package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.utils.Utilities;
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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = MerchantRepositoryExtendedImpl.class)
class MerchantRepositoryExtendedImplTest {
  @Autowired
  MerchantRepositoryExtended merchantRepositoryExtended;
  @MockBean MongoTemplate mongoTemplate;
  @MockBean Utilities utilitiesMock;
  @MockBean
  Criteria criteria;

  private static final String INITIATIVE_ID = "TEST_INITIATIVE_ID";
  private static final String FISCAL_CODE = "FISCAL_CODE";

  @Test
  void findByFilter() {
    Criteria criteria = new Criteria();
    Pageable paging = PageRequest.of(0, 20, Sort.by("updateDate"));
    when(utilitiesMock.getPageable(Mockito.any())).thenReturn(paging);
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
    Criteria criteria = merchantRepositoryExtended.getCriteria(INITIATIVE_ID, FISCAL_CODE);
    assertEquals(2, criteria.getCriteriaObject().size());
  }

  @Test
  void getCriteriaWithoutOperationType() {
    Criteria criteria = merchantRepositoryExtended.getCriteria(INITIATIVE_ID, null);
    assertEquals(1, criteria.getCriteriaObject().size());
  }


}
