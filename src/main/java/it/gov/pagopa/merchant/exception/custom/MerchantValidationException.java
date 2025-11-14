package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.dto.MerchantValidationErrorDetail;
import java.util.List;
import lombok.Getter;

@Getter
public class MerchantValidationException extends RuntimeException {

  private final List<MerchantValidationErrorDetail> errors;

  public MerchantValidationException(List<MerchantValidationErrorDetail> errors){
    super("Validation failed for one or more prerequisites");
    this.errors = errors;
  }
}
