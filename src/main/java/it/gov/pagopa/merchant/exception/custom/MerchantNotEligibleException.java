package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.constants.MerchantConstants;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.MERCHANT_NOT_ELIGIBLE;
import static it.gov.pagopa.merchant.constants.PointOfSaleConstants.MERCHANT_NOT_ALLOWED;

public class MerchantNotEligibleException extends ServiceException {

  public MerchantNotEligibleException(String message, boolean printStackTrace, Throwable ex) {
    this(MERCHANT_NOT_ELIGIBLE, message,printStackTrace, ex);
  }
  public MerchantNotEligibleException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message, null, printStackTrace, ex);
  }

  public MerchantNotEligibleException(String message) {
      this(MERCHANT_NOT_ELIGIBLE, message);

  }
  public MerchantNotEligibleException(String code, String message) {
    this(code, message, false,null);
  }

}
