package it.gov.pagopa.merchant.exception.custom;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.GENERIC_ERROR;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

public class TransactionInvocationException extends ServiceException {

  public TransactionInvocationException(String message) {
    this(GENERIC_ERROR, message);
  }

  public TransactionInvocationException(String message, boolean printStackTrace, Throwable ex) {
    this(GENERIC_ERROR, message, null, printStackTrace, ex);
  }

  public TransactionInvocationException(String code, String message) {
    this(code, message, null, false, null);
  }

  public TransactionInvocationException(String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
    super(code, message, payload, printStackTrace, ex);
  }
}
