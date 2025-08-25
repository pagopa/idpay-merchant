package it.gov.pagopa.merchant.event.consumer;

import static org.mockito.Mockito.verify;

import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import it.gov.pagopa.merchant.service.MerchantService;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class CommandsConsumerTest {
    @Mock
    private MerchantService merchantService;

    @InjectMocks
    private CommandsConsumer commandsConsumer;

    private Consumer<QueueCommandOperationDTO> consumerCommands;

    private static final String OPERATION_TYPE = "TESTOPERATIONTYPE";
    private static final String ENTITY_ID = "ENTITYID";
    private static final LocalDateTime OPERATION_TIME = LocalDateTime.now();

    @BeforeEach
    void setUp(){
        consumerCommands = commandsConsumer.consumerCommands(merchantService);
    }

    @Test
    void testConsumerCommands(){
        QueueCommandOperationDTO queueCommandOperationDTO = new QueueCommandOperationDTO(OPERATION_TYPE,ENTITY_ID,OPERATION_TIME);
        consumerCommands.accept(queueCommandOperationDTO);
        verify(merchantService).processOperation(queueCommandOperationDTO);

    }

}