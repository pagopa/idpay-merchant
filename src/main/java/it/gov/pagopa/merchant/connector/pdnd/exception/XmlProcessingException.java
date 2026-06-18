package it.gov.pagopa.merchant.connector.pdnd.exception;

public class XmlProcessingException extends RuntimeException {
    public XmlProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}