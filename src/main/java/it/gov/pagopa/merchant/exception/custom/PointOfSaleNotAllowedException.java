package it.gov.pagopa.merchant.exception.custom;

import static it.gov.pagopa.merchant.constants.PointOfSaleConstants.POINT_OF_SALE_NOT_ALLOWED;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

public class PointOfSaleNotAllowedException extends ServiceException {

  public PointOfSaleNotAllowedException(String message) {
    this(POINT_OF_SALE_NOT_ALLOWED, message);
  }

  public PointOfSaleNotAllowedException(String message, boolean printStackTrace, Throwable ex) {
    this(POINT_OF_SALE_NOT_ALLOWED, message, null, printStackTrace, ex);
  }

  public PointOfSaleNotAllowedException(String code, String message) {
    this(code, message, null, false, null);
  }

  public PointOfSaleNotAllowedException(
      String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
    super(code, message, payload, printStackTrace, ex);
  }
}