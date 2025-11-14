package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
import lombok.Getter;

import java.util.List;

@Getter
public class PosValidationException extends RuntimeException{

  private final List<ValidationErrorDetail> errors;

  public PosValidationException(List<ValidationErrorDetail> errors){
    super("Validation failed for one or more point of sales");
    this.errors = errors;
  }

}
