package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
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

    private static final int PAGE_SIZE = 100;

    @BeforeEach
    public void setUp() {
        merchantProcessOperationService = new MerchantProcessOperationServiceImpl(merchantFileRepository, repositoryMock
                , auditUtilities,PAGE_SIZE, 1000);
    }

    @ParameterizedTest
    @MethodSource("operationTypeAndInvocationTimes")
    void processOperation_deleteOperation(String operationType, int times) {

        QueueCommandOperationDTO queueCommandOperationDTO = QueueCommandOperationDTO.builder()
                .entityId(INITIATIVE_ID)
                .operationType(operationType)
                .operationTime(LocalDateTime.now())
                .build();

        Merchant merchant = MerchantFaker.mockInstance(1);
        List<Merchant> merchantList = List.of(merchant);

        MerchantFile merchantFile = MerchantFileFaker.mockInstance(1);
        List<MerchantFile> deletedMerchantFile = List.of(merchantFile);

        if (times == 2) {

            List<Merchant> merchants = createMerchantPage();

            Mockito.lenient().when(repositoryMock.findByInitiativeIdPageable(queueCommandOperationDTO.getEntityId(),
                            PAGE_SIZE))
                    .thenReturn(merchants)
                    .thenReturn(merchantList);

            List<MerchantFile> userGroupPage = createMerchantFilePage();

            Mockito.lenient().when(merchantFileRepository.deletePaged(queueCommandOperationDTO.getEntityId(),
                            PAGE_SIZE))
                    .thenReturn(userGroupPage)
                    .thenReturn(deletedMerchantFile);

            Thread.currentThread().interrupt();
        } else {
            Mockito.lenient().when(repositoryMock.findByInitiativeIdPageable(queueCommandOperationDTO.getEntityId(),
                            PAGE_SIZE))
                    .thenReturn(merchantList);

            Mockito.lenient().when(merchantFileRepository.deletePaged(queueCommandOperationDTO.getEntityId(),
                            PAGE_SIZE))
                    .thenReturn(deletedMerchantFile);
        }


        merchantProcessOperationService.processOperation(queueCommandOperationDTO);

        Mockito.verify(repositoryMock, Mockito.times(times)).findByInitiativeIdPageable(queueCommandOperationDTO.getEntityId(),
                PAGE_SIZE);
        Mockito.verify(merchantFileRepository, Mockito.times(times)).deletePaged(queueCommandOperationDTO.getEntityId(),
                PAGE_SIZE);
    }

    private static Stream<Arguments> operationTypeAndInvocationTimes() {
        return Stream.of(
                Arguments.of(OPERATION_TYPE_DELETE_INITIATIVE, 1),
                Arguments.of(OPERATION_TYPE_DELETE_INITIATIVE, 2),
                Arguments.of("OPERATION_TYPE_TEST", 0)
        );
    }
    private List<MerchantFile> createMerchantFilePage(){
        List<MerchantFile> merchantFilePage = new ArrayList<>();

        for(int i = 0; i< MerchantProcessOperationServiceImplTest.PAGE_SIZE; i++){
            merchantFilePage.add(MerchantFile.builder()
                    .id("MERCHNT_ID"+i)
                    .initiativeId(INITIATIVE_ID)
                    .build());
        }

        return merchantFilePage;
    }

    private List<Merchant> createMerchantPage(){
        List<Merchant> merchantPage = new ArrayList<>();

        Initiative initiative = Initiative.builder()
                .initiativeId(INITIATIVE_ID)
                .build();

        for(int i = 0; i< MerchantProcessOperationServiceImplTest.PAGE_SIZE; i++){
            merchantPage.add(Merchant.builder()
                    .merchantId("MERCHNT_ID"+i)
                    .initiativeList(List.of(initiative))
                    .build());
        }

        return merchantPage;
    }
}