package it.gov.pagopa.merchant.event.consumer;

import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import it.gov.pagopa.merchant.service.MerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class CommandsConsumerTest {
    @Mock
    private MerchantService merchantService;

    @InjectMocks
    private CommandsConsumer commandsConsumer;

    private Consumer<QueueCommandOperationDTO> consumerCommands;

    private final static String OPERATION_TYPE = "TESTOPERATIONTYPE";
    private final static  String ENTITY_ID = "ENTITYID";
    private final static LocalDateTime OPERATION_TIME = LocalDateTime.now();

    @BeforeEach
    public void setUp(){
        consumerCommands = commandsConsumer.consumerCommands(merchantService);
    }

    @Test
    void testConsumerCommands(){
        QueueCommandOperationDTO queueCommandOperationDTO = new QueueCommandOperationDTO(OPERATION_TYPE,ENTITY_ID,OPERATION_TIME);
        consumerCommands.accept(queueCommandOperationDTO);
        verify(merchantService).processOperation(queueCommandOperationDTO);

    }

}