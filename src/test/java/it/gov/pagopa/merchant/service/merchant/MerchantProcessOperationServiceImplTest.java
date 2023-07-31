package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.MerchantFile;
import it.gov.pagopa.merchant.repository.MerchantFileRepository;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.test.fakers.InitiativeFaker;
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
                .operationId(INITIATIVE_ID)
                .operationType(operationType)
                .build();

        Merchant merchant = MerchantFaker.mockInstance(1);
        merchant.setInitiativeList((List.of(
                InitiativeFaker.mockInstance(1),
                InitiativeFaker.mockInstance(2))));
        List<Merchant> deletedMerchant = List.of(merchant);

        MerchantFile merchantFile = MerchantFileFaker.mockInstance(1);
        List<MerchantFile> deletedMerchantFile = List.of(merchantFile);

        //Mockito.lenient().when(repositoryMock.deleteByInitiativeId(queueCommandOperationDTO.getOperationId()))
          //      .thenReturn(deletedMerchant);

        Mockito.lenient().when(merchantFileRepository.deleteByInitiativeId(queueCommandOperationDTO.getOperationId()))
                .thenReturn(deletedMerchantFile);

        merchantProcessOperationService.processOperation(queueCommandOperationDTO);

        Mockito.verify(repositoryMock, Mockito.times(times)).deleteByInitiativeId(queueCommandOperationDTO.getOperationId());
        Mockito.verify(merchantFileRepository, Mockito.times(times)).deleteByInitiativeId(queueCommandOperationDTO.getOperationId());
    }

    private static Stream<Arguments> operationTypeAndInvocationTimes() {
        return Stream.of(
                Arguments.of(OPERATION_TYPE_DELETE_INITIATIVE, 1),
                Arguments.of("OPERATION_TYPE_TEST", 0)
        );
    }
}