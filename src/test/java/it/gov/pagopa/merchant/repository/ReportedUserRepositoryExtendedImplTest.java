package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.ReportedUser;
import it.gov.pagopa.merchant.utils.Utilities;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.client.result.DeleteResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportedUserRepositoryExtendedImplTest {

    @org.mockito.Mock
    private MongoTemplate mongoTemplate;

    @Test
    void findByFilter_withNulls_callsFindWithEmptyQuery() {
        var repo = new ReportedUserRepositoryExtendedImpl(mongoTemplate);
        when(mongoTemplate.find(any(Query.class), eq(ReportedUser.class))).thenReturn(List.of());

        List<ReportedUser> out = repo.findByFilter(null, null);

        assertNotNull(out);
        assertTrue(out.isEmpty());

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(ReportedUser.class));
        Query q = captor.getValue();
        assertEquals(0, q.getQueryObject().size(), "Query senza criteri");
        assertEquals(0, q.getSkip());
        assertEquals(0, q.getLimit());
    }

    @Test
    void findByFilter_withCriteriaAndPageable_appliesBoth() {
        var repo = new ReportedUserRepositoryExtendedImpl(mongoTemplate);
        when(mongoTemplate.find(any(Query.class), eq(ReportedUser.class))).thenReturn(List.of(
                ReportedUser.builder().reportedUserId("RID1").build()
        ));

        Criteria c = Criteria.where(ReportedUser.Fields.merchantId).is("M1");
        Pageable inPageable = PageRequest.of(2, 5);

        try (MockedStatic<Utilities> util = mockStatic(Utilities.class)) {
            util.when(() -> Utilities.getPageable(inPageable)).thenReturn(inPageable);

            List<ReportedUser> out = repo.findByFilter(c, inPageable);

            assertEquals(1, out.size());

            ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
            verify(mongoTemplate).find(captor.capture(), eq(ReportedUser.class));
            Query q = captor.getValue();

            Document qo = q.getQueryObject();
            assertEquals("M1", ((Document) qo).getString(ReportedUser.Fields.merchantId));
            assertEquals(10L, q.getSkip());
            assertEquals(5, q.getLimit());
        }
    }

    @Test
    void getCriteria_allNull_returnsEmptyCriteria() {
        var repo = new ReportedUserRepositoryExtendedImpl(mongoTemplate);
        Criteria c = repo.getCriteria(null, null, null);

        Query q = new Query().addCriteria(c);
        assertEquals(0, q.getQueryObject().size());
    }

    @Test
    void getCriteria_singleFields_buildsAndOperatorWithOneCondition() {
        var repo = new ReportedUserRepositoryExtendedImpl(mongoTemplate);

        Criteria c1 = repo.getCriteria("M1", null, null);
        Document d1 = new Query().addCriteria(c1).getQueryObject();
        assertTrue(d1.containsKey("$and"));
        var and1 = (List<Document>) d1.get("$and");
        assertEquals(1, and1.size());
        assertEquals("M1", and1.get(0).getString(ReportedUser.Fields.merchantId));

        Criteria c2 = repo.getCriteria(null, "I1", null);
        Document d2 = new Query().addCriteria(c2).getQueryObject();
        var and2 = (List<Document>) d2.get("$and");
        assertEquals(1, and2.size());
        assertEquals("I1", and2.get(0).getString(ReportedUser.Fields.initiativeId));

        Criteria c3 = repo.getCriteria(null, null, "U1");
        Document d3 = new Query().addCriteria(c3).getQueryObject();
        var and3 = (List<Document>) d3.get("$and");
        assertEquals(1, and3.size());
        assertEquals("U1", and3.get(0).getString(ReportedUser.Fields.userId));
    }

    @Test
    void getCriteria_multipleFields_buildsAndOperatorWithAllConditions() {
        var repo = new ReportedUserRepositoryExtendedImpl(mongoTemplate);

        Criteria c = repo.getCriteria("M1", "I1", "U1");
        Document d = new Query().addCriteria(c).getQueryObject();

        assertTrue(d.containsKey("$and"));
        var and = (List<Document>) d.get("$and");
        assertEquals(3, and.size());

        assertEquals("M1", and.get(0).getString(ReportedUser.Fields.merchantId));
        assertEquals("I1", and.get(1).getString(ReportedUser.Fields.initiativeId));
        assertEquals("U1", and.get(2).getString(ReportedUser.Fields.userId));
    }

    @Test
    void getCount_withAndWithoutCriteria_callsMongoTemplateCount() {
        var repo = new ReportedUserRepositoryExtendedImpl(mongoTemplate);

        when(mongoTemplate.count(any(Query.class), eq(ReportedUser.class))).thenReturn(42L);

        long count = repo.getCount(null);
        assertEquals(42L, count);

        ArgumentCaptor<Query> captor1 = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor1.capture(), eq(ReportedUser.class));
        assertEquals(0, captor1.getValue().getQueryObject().size());

        reset(mongoTemplate);
        when(mongoTemplate.count(any(Query.class), eq(ReportedUser.class))).thenReturn(7L);

        Criteria c = Criteria.where(ReportedUser.Fields.userId).is("U1");
        long count2 = repo.getCount(c);
        assertEquals(7L, count2);

        ArgumentCaptor<Query> captor2 = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor2.capture(), eq(ReportedUser.class));
        assertEquals("U1", captor2.getValue().getQueryObject().getString(ReportedUser.Fields.userId));
    }

    @Test
    void deleteByUserId_nullOrBlank_shortCircuitsAndReturnsZero() {
        var repo = new ReportedUserRepositoryExtendedImpl(mongoTemplate);

        assertEquals(0L, repo.deleteByUserId(null));
        assertEquals(0L, repo.deleteByUserId(""));
        assertEquals(0L, repo.deleteByUserId("   "));

        verify(mongoTemplate, never()).remove(any(Query.class), eq(ReportedUser.class));
    }

    @Test
    void deleteByUserId_valid_callsRemoveAndReturnsDeletedCount() {
        var repo = new ReportedUserRepositoryExtendedImpl(mongoTemplate);

        DeleteResult res = mock(DeleteResult.class);
        when(res.getDeletedCount()).thenReturn(3L);
        when(mongoTemplate.remove(any(Query.class), eq(ReportedUser.class))).thenReturn(res);

        long deleted = repo.deleteByUserId("U1");

        assertEquals(3L, deleted);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).remove(captor.capture(), eq(ReportedUser.class));

        Document qo = captor.getValue().getQueryObject();
        assertEquals("U1", qo.getString(ReportedUser.Fields.userId));
    }
}
