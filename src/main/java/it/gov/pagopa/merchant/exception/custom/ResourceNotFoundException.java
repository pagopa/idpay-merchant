package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.RESOURCE_NOT_FOUND;

public class ResourceNotFoundException extends ServiceException {
    public ResourceNotFoundException(String message) {
        this(RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(String message, boolean printStackTrace, Throwable ex) {
        this(RESOURCE_NOT_FOUND, message, null, printStackTrace, ex);
    }

    public ResourceNotFoundException(String code, String message) {
        this(code, message, null, false, null);
    }

    public ResourceNotFoundException(
            String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
        super(code, message, payload, printStackTrace, ex);
    }
}
