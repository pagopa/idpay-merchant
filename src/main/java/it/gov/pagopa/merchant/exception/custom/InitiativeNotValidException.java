package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.INITIATIVE_NOT_VALID;

public class InitiativeNotValidException extends ServiceException {

  public InitiativeNotValidException(String message) {
    this(INITIATIVE_NOT_VALID, message);
  }

  public InitiativeNotValidException(String message, boolean printStackTrace, Throwable ex) {
    this(INITIATIVE_NOT_VALID, message, null, printStackTrace, ex);
  }

  public InitiativeNotValidException(String code, String message) {
    this(code, message, null, false, null);
  }

  public InitiativeNotValidException(
      String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
    super(code, message, payload, printStackTrace, ex);
  }
}