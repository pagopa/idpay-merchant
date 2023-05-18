package it.gov.pagopa.merchant.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class MerchantException extends RuntimeException{
    private final String code;

    private final String message;

    private final HttpStatus httpStatus;
}
