package it.gov.pagopa.merchant.exception.custom;

import static it.gov.pagopa.merchant.constants.PointOfSaleConstants.MERCHANT_NOT_ALLOWED;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

public class MerchantNotAllowedException extends ServiceException {

  public MerchantNotAllowedException(String message) {
    this(MERCHANT_NOT_ALLOWED, message);
  }

  public MerchantNotAllowedException(String message, boolean printStackTrace, Throwable ex) {
    this(MERCHANT_NOT_ALLOWED, message, null, printStackTrace, ex);
  }

  public MerchantNotAllowedException(String code, String message) {
    this(code, message, null, false, null);
  }

  public MerchantNotAllowedException(
      String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
    super(code, message, payload, printStackTrace, ex);
  }
}