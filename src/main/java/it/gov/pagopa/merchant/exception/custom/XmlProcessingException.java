package it.gov.pagopa.merchant.exception.custom;

public class XmlProcessingException extends RuntimeException {
    public XmlProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}