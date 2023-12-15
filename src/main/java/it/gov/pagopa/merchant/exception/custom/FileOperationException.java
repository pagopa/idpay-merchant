package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.GENERIC_ERROR;

public class FileOperationException extends ServiceException {

    public FileOperationException(String message) {
        this(GENERIC_ERROR, message);
    }

    public FileOperationException(String code, String message) {
        this(code, message, false, null);
    }

    public FileOperationException(String code, String message, boolean printStackTrace, Throwable ex) {
        super(code, message, printStackTrace, ex);
    }
}
