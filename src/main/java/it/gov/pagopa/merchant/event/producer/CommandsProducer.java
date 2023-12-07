package it.gov.pagopa.merchant.event.producer;

import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandsProducer {
    private static final String COMMANDS_QUEUE_OUT_0 = "commandsQueue-out-0";

    private final StreamBridge streamBridge;

    public CommandsProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public boolean sendCommand(QueueCommandOperationDTO queueCommandOperationDTO){
        log.debug("Sending Command Operation to {}", COMMANDS_QUEUE_OUT_0);
        return streamBridge.send(COMMANDS_QUEUE_OUT_0, queueCommandOperationDTO);
    }

}
