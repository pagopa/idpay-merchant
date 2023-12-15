package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.MERCHANT_NOT_FOUND;


public class MerchantNotFoundException extends ServiceException {

    public MerchantNotFoundException(String message) {
        this(MERCHANT_NOT_FOUND, message);
    }

    public MerchantNotFoundException(String code, String message) {
        this(code, message, false, null);
    }

    public MerchantNotFoundException(String code, String message, boolean printStackTrace, Throwable ex) {
        super(code, message, printStackTrace, ex);
    }
}
