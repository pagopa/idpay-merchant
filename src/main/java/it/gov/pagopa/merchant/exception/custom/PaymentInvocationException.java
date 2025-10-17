package it.gov.pagopa.merchant.exception.custom;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.GENERIC_ERROR;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

public class PaymentInvocationException extends ServiceException {

  public PaymentInvocationException(String message) {
    this(GENERIC_ERROR, message);
  }

  public PaymentInvocationException(String message, boolean printStackTrace, Throwable ex) {
    this(GENERIC_ERROR, message, null, printStackTrace, ex);
  }

  public PaymentInvocationException(String code, String message) {
    this(code, message, null, false, null);
  }

  public PaymentInvocationException(String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
    super(code, message, payload, printStackTrace, ex);
  }
}
