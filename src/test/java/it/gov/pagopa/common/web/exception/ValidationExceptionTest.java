package it.gov.pagopa.common.web.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ValidationExceptionTest {
    private final static String MESSAGE = "TestMessage";
    private final static Throwable EXCEPTION = new RuntimeException("TestException");

    @Test
    void testConstructorWithHttpStatusMessageAndThrowable() {

        ValidationException exception = new ValidationException(List.of());

        assertEquals("Validation failed for one or more point of sales", exception.getMessage());
        assertNotNull(exception.getErrors());
    }


}