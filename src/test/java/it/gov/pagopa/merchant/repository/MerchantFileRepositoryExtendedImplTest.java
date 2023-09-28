package it.gov.pagopa.merchant.repository;

import it.gov.pagopa.merchant.model.MerchantFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = MerchantFileRepositoryExtendedImpl.class)
class MerchantFileRepositoryExtendedImplTest {
    @Autowired
    MerchantFileRepositoryExtended merchantFileRepositoryExtended;
    @MockBean
    MongoTemplate mongoTemplate;

    private static final String INITIATIVE_ID = "initiativeId";
    private static final String FILENAME = "fileName";

    private static final String STATUS = "status";

    @Test
    void setMerchantFileStatus () {
        Query query = new Query();
        query.addCriteria(Criteria.where(INITIATIVE_ID).is(INITIATIVE_ID).and(FILENAME).is(FILENAME));

        Update update = new Update();
        update.set(STATUS, STATUS);

        merchantFileRepositoryExtended.setMerchantFileStatus(INITIATIVE_ID,FILENAME,STATUS);

        Mockito.verify(mongoTemplate, Mockito.times(1)).updateFirst(query,update,MerchantFile.class);
    }
    @Test
    void deletePaged() {

        String initiativeId = "initiativeId";
        int pageSize = 2;

        MerchantFile file = MerchantFile.builder().initiativeId("initiativeId1").build();

        List<MerchantFile> fileList = List.of(file);

        Mockito.when(mongoTemplate.findAllAndRemove(Mockito.any(Query.class), Mockito.eq(MerchantFile.class)))
                .thenReturn(fileList);

        List<MerchantFile> deletedGroups = merchantFileRepositoryExtended.deletePaged(initiativeId, pageSize);

        Mockito.verify(mongoTemplate, Mockito.times(1)).findAllAndRemove(Mockito.any(Query.class),Mockito.eq(MerchantFile.class));

        Assertions.assertEquals(fileList, deletedGroups);
    }

}