package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.MERCHANT_ALREDY_ONBORDED;

public class MerchantAlreadyOnboardedException extends ServiceException {

  public MerchantAlreadyOnboardedException(String message, boolean printStackTrace, Throwable ex) {
    this(MERCHANT_ALREDY_ONBORDED, message,printStackTrace, ex);
  }
  public MerchantAlreadyOnboardedException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message, null, printStackTrace, ex);
  }

  public MerchantAlreadyOnboardedException(String message) {
      this(MERCHANT_ALREDY_ONBORDED, message);
  }

  public MerchantAlreadyOnboardedException(String code, String message) {
    this(code, message, false, null);
  }
}
