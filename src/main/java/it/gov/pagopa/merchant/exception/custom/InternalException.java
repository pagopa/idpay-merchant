package it.gov.pagopa.merchant.exception.custom;

public class InternalException extends RuntimeException{

    public InternalException(Throwable cause) {
        super(cause);
    }

    public InternalException(){
        super();
    }

    public InternalException(String message){
        super(message);
    }

}
