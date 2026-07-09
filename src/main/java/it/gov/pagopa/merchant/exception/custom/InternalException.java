package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.INTERNAL_ERROR;

public class InternalException extends ServiceException {

    public InternalException(String message) {
        this(INTERNAL_ERROR, message);
    }

    public InternalException(String message, boolean printStackTrace, Throwable ex) {
        this(INTERNAL_ERROR, message, null, printStackTrace, ex);
    }

    public InternalException(String code, String message) {
        this(code, message, null, false, null);
    }

    public InternalException(
            String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
        super(code, message, payload, printStackTrace, ex);
    }

}
