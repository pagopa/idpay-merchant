package it.gov.pagopa.merchant.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

import static it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.XML_PROCESSING_ERROR;

public class XmlProcessingException extends ServiceException {


    public XmlProcessingException(String message) {
        this(XML_PROCESSING_ERROR, message);
    }

    public XmlProcessingException(String message, boolean printStackTrace, Throwable cause) {
        this(XML_PROCESSING_ERROR, message, null, printStackTrace, cause);
    }

    public XmlProcessingException(String code, String message) {
        this(code, message, null, false, null);
    }

    public XmlProcessingException(
            String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable cause) {
        super(code, message, payload, printStackTrace, cause);
    }
}