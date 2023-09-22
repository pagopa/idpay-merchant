package it.gov.pagopa.merchant.service.merchant;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.MerchantFileFaker;
import it.gov.pagopa.merchant.utils.AuditUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("pagination", "2");
        additionalParams.put("delay", "1");

        QueueCommandOperationDTO queueCommandOperationDTO = QueueCommandOperationDTO.builder()
                .entityId(INITIATIVE_ID)
                .operationType(operationType)
                .operationTime(LocalDateTime.now())
                .additionalParams(additionalParams)
                .build();

        UpdateResult updateResult = UpdateResult.acknowledged(0,1L,null);
        UpdateResult updateResultGT = UpdateResult.acknowledged(0,2L,null);

        MerchantFile merchantFile = MerchantFileFaker.mockInstance(1);
        List<MerchantFile> deletedMerchantFile = List.of(merchantFile);

        if (times == 2) {
            Mockito.lenient().when(repositoryMock.findAndRemoveInitiativeOnMerchant(queueCommandOperationDTO.getEntityId(),
                            Integer.parseInt(queueCommandOperationDTO.getAdditionalParams().get("pagination"))))
                    .thenReturn(updateResultGT)
                    .thenReturn(updateResult);

            List<MerchantFile> userGroupPage = createMerchantFilePage(Integer.parseInt("2"));

            Mockito.lenient().when(merchantFileRepository.deletePaged(queueCommandOperationDTO.getEntityId(),
                            Integer.parseInt(queueCommandOperationDTO.getAdditionalParams().get("pagination"))))
                    .thenReturn(userGroupPage)
                    .thenReturn(deletedMerchantFile);

            Thread.currentThread().interrupt();
        } else {
            Mockito.lenient().when(repositoryMock.findAndRemoveInitiativeOnMerchant(queueCommandOperationDTO.getEntityId(),
                            Integer.parseInt(queueCommandOperationDTO.getAdditionalParams().get("pagination"))))
                    .thenReturn(updateResult);

            Mockito.lenient().when(merchantFileRepository.deletePaged(queueCommandOperationDTO.getEntityId(),
                            Integer.parseInt(queueCommandOperationDTO.getAdditionalParams().get("pagination"))))
                    .thenReturn(deletedMerchantFile);
        }


        merchantProcessOperationService.processOperation(queueCommandOperationDTO);

        Mockito.verify(repositoryMock, Mockito.times(times)).findAndRemoveInitiativeOnMerchant(queueCommandOperationDTO.getEntityId(),
                Integer.parseInt(queueCommandOperationDTO.getAdditionalParams().get("pagination")));
        Mockito.verify(merchantFileRepository, Mockito.times(times)).deletePaged(queueCommandOperationDTO.getEntityId(),
                Integer.parseInt(queueCommandOperationDTO.getAdditionalParams().get("pagination")));
    }

    private static Stream<Arguments> operationTypeAndInvocationTimes() {
        return Stream.of(
                Arguments.of(OPERATION_TYPE_DELETE_INITIATIVE, 1),
                Arguments.of(OPERATION_TYPE_DELETE_INITIATIVE, 2),
                Arguments.of("OPERATION_TYPE_TEST", 0)
        );
    }
    private List<MerchantFile> createMerchantFilePage(int pageSize){
        List<MerchantFile> merchantFilePage = new ArrayList<>();

        for(int i=0;i<pageSize; i++){
            merchantFilePage.add(MerchantFile.builder()
                    .id("MERCHNT_ID"+i)
                    .initiativeId(INITIATIVE_ID)
                    .build());
        }

        return merchantFilePage;
    }
}