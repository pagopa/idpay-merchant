package it.gov.pagopa.common.kafka;

import com.fasterxml.jackson.databind.ObjectReader;
import it.gov.pagopa.common.kafka.utils.CommonUtilities;
import it.gov.pagopa.common.kafka.utils.KafkaConstants;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import reactor.util.context.Context;

@Slf4j
public abstract class BaseKafkaConsumer<T> {

    /** Key used inside the {@link Context} to store the startTime */
    protected static final String CONTEXT_KEY_START_TIME = "START_TIME";
    /** Key used inside the {@link Context} to store a msg identifier used for logging purpose */
    protected static final String CONTEXT_KEY_MSG_ID = "MSG_ID";

    private final String applicationName;

    protected BaseKafkaConsumer(String applicationName) {
        this.applicationName = applicationName;
    }

    public void execute(Message<String> message) {
        Map<String, Object> ctx=new HashMap<>();
        ctx.put(CONTEXT_KEY_START_TIME, System.currentTimeMillis());
        ctx.put(CONTEXT_KEY_MSG_ID, message.getPayload());

        if (!isRetryFromOtherApps(message)) {
            T payload = deserializeMessage(message);
            if(payload != null){
                try {
                    execute(payload, message);
                }catch (RuntimeException e){
                    onError(message, e);
                }
            }
        }

        acknowledgeMessage(message);
        doFinally(message,ctx);
    }

    protected abstract void onError(Message<String> message, Throwable e);

    private boolean isRetryFromOtherApps(Message<String> message) {
        byte[] retryingApplicationName = message.getHeaders().get(KafkaConstants.ERROR_MSG_HEADER_APPLICATION_NAME, byte[].class);
        if(retryingApplicationName != null && !new String(retryingApplicationName, StandardCharsets.UTF_8).equals(this.applicationName)){
            log.info("[{}] Discarding message due to other application retry ({}): {}", getFlowName(), new String(retryingApplicationName, StandardCharsets.UTF_8), message.getPayload());
            return true;
        }
        return false;
    }

    private void acknowledgeMessage(Message<String> message) {
        Acknowledgment ack = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
        if (ack != null) {
            ack.acknowledge();
        }
    }

    /** Name used for logging purpose */
    protected String getFlowName() {
        return getClass().getSimpleName();
    }

    /** The {@link ObjectReader} to use in order to deserialize the input message */
    protected abstract ObjectReader getObjectReader();

    /**
     * The action to take if the deserialization will throw an error
     */
    protected abstract void onDeserializationError(Message<String> message, Throwable e);

    protected T deserializeMessage(Message<String> message) {
        return CommonUtilities.deserializeMessage(message, getObjectReader(), e -> onDeserializationError(message, e));
    }

    /** The function invoked in order to process the current message */
    protected abstract void execute(T payload, Message<String> message);

    /** to perform some operation at the end of business logic execution, thus before to wait for commit. As default, it will perform an INFO logging with performance time */
    @SuppressWarnings("sonar:S1172") // suppressing unused parameters
    protected void doFinally(Message<String> message, Map<String, Object> ctx) {
        Long startTime = (Long)ctx.get(CONTEXT_KEY_START_TIME);
        String msgId = (String)ctx.get(CONTEXT_KEY_MSG_ID);
        if(startTime != null){
            log.info("[PERFORMANCE_LOG] [{}] Time occurred to perform business logic: {} ms {}", getFlowName(), System.currentTimeMillis() - startTime, msgId);
        }
    }
}
