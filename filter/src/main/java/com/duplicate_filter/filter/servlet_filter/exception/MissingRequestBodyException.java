package com.duplicate_filter.filter.servlet_filter.exception;

public class MissingRequestBodyException extends Exception {
    
    public MissingRequestBodyException(){
        super();
    }

    public MissingRequestBodyException(String message){
        super(message);
    }

    public MissingRequestBodyException(String message, Throwable cause){
        super(message, cause);
    }

    public MissingRequestBodyException(Throwable cause){
        super(cause);
    }
}
