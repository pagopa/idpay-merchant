package it.gov.pagopa.common.web.exception;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException{

  private final List<ValidationErrorDetail> errors;

  public ValidationException(List<ValidationErrorDetail> errors){
    super("Validation failed for one or more point of sales");
    this.errors = errors;
  }

}
