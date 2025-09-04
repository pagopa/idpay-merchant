package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.GENERIC_ERROR;

public class MerchantAlreadyExistsException extends ServiceException {

  public MerchantAlreadyExistsException(String message) {
    this(GENERIC_ERROR, message);
  }

  public MerchantAlreadyExistsException(String code, String message) {
    this(code, message, null, false, null);
  }

  public MerchantAlreadyExistsException(String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
    super(code, message, payload, printStackTrace, ex);
  }
}
