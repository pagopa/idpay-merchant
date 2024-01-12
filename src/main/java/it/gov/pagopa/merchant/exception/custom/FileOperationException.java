package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.GENERIC_ERROR;

public class FileOperationException extends ServiceException {

    public FileOperationException(String message, Throwable ex) {
        this(GENERIC_ERROR, message, null, true, ex);
    }

    public FileOperationException(String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
        super(code, message, payload, printStackTrace, ex);
    }
}
