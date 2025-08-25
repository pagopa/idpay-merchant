package it.gov.pagopa.merchant.event.producer;

import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class CommandsProducerTest {
    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private CommandsProducer commandsProducer;

    private static final String OPERATION_TYPE = "TESTOPERATIONTYPE";
    private static final String ENTITY_ID = "ENTITYID";
    private static final LocalDateTime OPERATION_TIME = LocalDateTime.now();
    private static final String COMMANDS_QUEUE_OUT_0 = "commandsQueue-out-0";

    @Test
    void testSendCommand() {
        QueueCommandOperationDTO commandDTO = new QueueCommandOperationDTO(OPERATION_TYPE, ENTITY_ID, OPERATION_TIME);

        commandsProducer.sendCommand(commandDTO);

        Mockito.verify(streamBridge).send(COMMANDS_QUEUE_OUT_0, commandDTO);
    }

}