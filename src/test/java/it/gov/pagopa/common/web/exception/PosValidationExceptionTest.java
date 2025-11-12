package it.gov.pagopa.common.web.exception;

import it.gov.pagopa.merchant.exception.custom.PosValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PosValidationExceptionTest {

    @Test
    void testConstructorWithHttpStatusMessageAndThrowable() {

        PosValidationException exception = new PosValidationException(List.of());

        assertEquals("Validation failed for one or more point of sales", exception.getMessage());
        assertNotNull(exception.getErrors());
    }


}