package it.gov.pagopa.common.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import it.gov.pagopa.merchant.exception.custom.MerchantValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MerchantValidationExceptionTest {

  @Test
  void testConstructorWithHttpStatusMessageAndThrowable() {

    MerchantValidationException exception = new MerchantValidationException(List.of());

    assertEquals("Validation failed for one or more prerequisites", exception.getMessage());
    assertNotNull(exception.getErrors());
  }
}
