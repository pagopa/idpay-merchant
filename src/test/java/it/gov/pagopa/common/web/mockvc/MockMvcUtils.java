package it.gov.pagopa.common.web.mockvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.common.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;


public final class MockMvcUtils {
    private MockMvcUtils() {}

    public static <T> T extractResponse(MvcResult response, HttpStatus expectedHttpStatusCode, Class<T> expectedBodyClass) {
        Assertions.assertEquals(expectedHttpStatusCode.value(), response.getResponse().getStatus());
        if (expectedBodyClass != null) {
            try {
                String body = response.getResponse().getContentAsString();
                if(String.class.equals(expectedBodyClass)){
                    //noinspection unchecked
                    return (T)body;
                } else {
                    return TestUtils.objectMapper.readValue(body, expectedBodyClass);
                }
            } catch (JsonProcessingException | UnsupportedEncodingException e) {
                throw new IllegalStateException("Cannot read body response!", e);
            }
        } else {
            return null;
        }
    }
}
