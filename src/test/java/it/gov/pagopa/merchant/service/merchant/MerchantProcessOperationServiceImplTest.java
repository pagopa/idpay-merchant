package it.gov.pagopa.merchant.service.merchant;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.MerchantFileFaker;
import it.gov.pagopa.merchant.utils.AuditUtilities;
import org.bson.BsonValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;
@ExtendWith(MockitoExtension.class)
class MerchantProcessOperationServiceImplTest {
    MerchantProcessOperationService merchantProcessOperationService;
    @Mock
    private MerchantRepository repositoryMock;
    @Mock
    private MerchantFileRepository merchantFileRepository;
    @Mock
    private AuditUtilities auditUtilities;

    private static final String INITIATIVE_ID = "INITIATIVEID1";
    private static final String OPERATION_TYPE_DELETE_INITIATIVE = "DELETE_INITIATIVE";

    @BeforeEach
    public void setUp() {
        merchantProcessOperationService = new MerchantProcessOperationServiceImpl(merchantFileRepository, repositoryMock
                , auditUtilities);
    }

    @ParameterizedTest
    @MethodSource("operationTypeAndInvocationTimes")
    void processOperation_deleteOperation(String operationType, int times) {
        QueueCommandOperationDTO queueCommandOperationDTO = QueueCommandOperationDTO.builder()
                .entityId(INITIATIVE_ID)
                .operationType(operationType)
                .build();

        UpdateResult updateResult = new UpdateResult() {
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

        MerchantFile merchantFile = MerchantFileFaker.mockInstance(1);
        List<MerchantFile> deletedMerchantFile = List.of(merchantFile);

        Mockito.lenient().when(repositoryMock.findAndRemoveInitiativeOnMerchant(queueCommandOperationDTO.getEntityId()))
                .thenReturn(updateResult);

        Mockito.lenient().when(merchantFileRepository.deleteByInitiativeId(queueCommandOperationDTO.getEntityId()))
                .thenReturn(deletedMerchantFile);

        merchantProcessOperationService.processOperation(queueCommandOperationDTO);

        Mockito.verify(repositoryMock, Mockito.times(times)).findAndRemoveInitiativeOnMerchant(queueCommandOperationDTO.getEntityId());
        Mockito.verify(merchantFileRepository, Mockito.times(times)).deleteByInitiativeId(queueCommandOperationDTO.getEntityId());
    }

    private static Stream<Arguments> operationTypeAndInvocationTimes() {
        return Stream.of(
                Arguments.of(OPERATION_TYPE_DELETE_INITIATIVE, 1),
                Arguments.of("OPERATION_TYPE_TEST", 0)
        );
    }
}