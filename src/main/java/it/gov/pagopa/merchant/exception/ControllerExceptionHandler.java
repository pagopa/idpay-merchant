package it.gov.pagopa.merchant.exception;

import it.gov.pagopa.merchant.dto.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
    @ExceptionHandler({MerchantException.class})
    public ResponseEntity<ErrorDTO> handleInitiativeException(MerchantException ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(new ErrorDTO(ex.getCode(), ex.getMessage()),
                ex.getHttpStatus());
    }
}
