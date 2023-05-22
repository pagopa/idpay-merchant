package it.gov.pagopa.merchant.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.merchant.configuration.JsonConfig;
import it.gov.pagopa.merchant.utils.Utilities;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class TestUtils {
    private TestUtils() {}

    static {
        TimeZone.setDefault(TimeZone.getTimeZone(Utilities.ZONEID));
    }

    /**
     * applications's objectMapper
     */
    public static ObjectMapper objectMapper = new JsonConfig().objectMapper();

    public static void checkNotNullFields(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    org.springframework.util.ReflectionUtils.doWithFields(o.getClass(),
        f -> {
          f.setAccessible(true);
          Assertions.assertNotNull(f.get(o), "The field %s of the input object of type %s is null!".formatted(f.getName(), o.getClass()));
        },
        f -> !excludedFieldsSet.contains(f.getName()));
    }

    /**
     * To serialize an object as a JSON handling Exception
     */
    public static String jsonSerializer(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * To deserialize an object as a JSON handling Exception
     */
    public static <T> T jsonDeserializer(String payload, Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * To read {@link Header} value
     */
    public static String getHeaderValue(ConsumerRecord<String, String> errorMessage, String errorMsgHeaderSrcServer) {
        Header header = errorMessage.headers().lastHeader(errorMsgHeaderSrcServer);
        return header!=null? new String(header.value()) : null;
    }

}
