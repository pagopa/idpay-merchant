package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.constants.MerchantConstants;

public class PDVInvocationException extends ServiceException {

  public PDVInvocationException(String message, boolean printStackTrace, Throwable ex) {
    this(MerchantConstants.ExceptionCode.GENERIC_ERROR, message,printStackTrace, ex);
  }
  public PDVInvocationException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message, null, printStackTrace, ex);
  }

}
