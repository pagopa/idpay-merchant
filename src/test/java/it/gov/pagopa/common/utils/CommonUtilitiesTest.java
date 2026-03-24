package it.gov.pagopa.common.utils;

import it.gov.pagopa.common.kafka.utils.CommonUtilities;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@Slf4j
class CommonUtilitiesTest {

    @Test
    void testDeserializeMessageCatch() {
        byte[] payloadBytes = "invalid-json".getBytes();
        Message<byte[]> message = MessageBuilder.withPayload(payloadBytes).build();
        ObjectMapper objectMapper = new ObjectMapper();
        DeserializationConfig deserializationConfig = mock(DeserializationConfig.class);
        ObjectReader objectReader = new ObjectReader(objectMapper, deserializationConfig) {
            @Override
            public Object readValue(String content) throws JacksonException {
                throw new JacksonException("test exception") {};
            }
        };
        AtomicReference<Throwable> capturedError = new AtomicReference<>();
        Consumer<Throwable> onError = capturedError::set;
        Object result = CommonUtilities.deserializeMessage(message, objectReader, onError);
        assertNull(result);
    }

    @Test
    void testReadMessagePayload(){
        byte[] payload = "Questo è un messaggio".getBytes();

        Message<byte[]> message = MessageBuilder.withPayload(payload)
                .setHeader("headerKey", "headerValue")
                .build();

        Assertions.assertNotNull(CommonUtilities.readMessagePayload(message));
    }

    @Test
    void testCentsToEuro(){
        Assertions.assertEquals(
                BigDecimal.valueOf(5).setScale(2, RoundingMode.UNNECESSARY),
                CommonUtilities.centsToEuro(5_00L)
        );
    }

    @Test
    void testEuroToCents(){
        assertNull(CommonUtilities.euroToCents(null));
        Assertions.assertEquals(100L, CommonUtilities.euroToCents(BigDecimal.ONE));
        Assertions.assertEquals(325L, CommonUtilities.euroToCents(BigDecimal.valueOf(3.25)));

        Assertions.assertEquals(
                5_00L,
                CommonUtilities.euroToCents(TestUtils.bigDecimalValue(5))
        );
    }

}
