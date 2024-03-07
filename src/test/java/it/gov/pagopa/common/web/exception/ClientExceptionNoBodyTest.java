package it.gov.pagopa.common.web.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
@ExtendWith(MockitoExtension.class)
class ClientExceptionNoBodyTest {
    private final static String MESSAGE = "TestMessage";
    private final static Throwable EXCEPTION = new RuntimeException("TestException");

    @Test
    void testConstructorWithHttpStatusMessageAndThrowable() {

        ClientExceptionNoBody exception = new ClientExceptionNoBody(HttpStatus.BAD_REQUEST, MESSAGE, EXCEPTION);

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(MESSAGE, exception.getMessage());
        assertEquals(EXCEPTION, exception.getCause());
    }

    @Test
    void testConstructorWithHttpStatusMessagePrintStackTraceAndThrowable() {

        ClientExceptionNoBody exception = new ClientExceptionNoBody(HttpStatus.BAD_REQUEST, MESSAGE, true, EXCEPTION);

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(MESSAGE, exception.getMessage());
        assertEquals(EXCEPTION, exception.getCause());
    }

}