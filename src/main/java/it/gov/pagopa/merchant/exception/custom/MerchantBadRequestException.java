package it.gov.pagopa.merchant.exception.custom;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.INVALID_REQUEST;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

public class MerchantBadRequestException extends ServiceException {

    public MerchantBadRequestException(String message) {
        this(INVALID_REQUEST, message);
    }

    public MerchantBadRequestException(String code, String message) {
        this(code, message, null, false, null);
    }

    public MerchantBadRequestException(String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
        super(code, message, payload, printStackTrace, ex);
    }
}