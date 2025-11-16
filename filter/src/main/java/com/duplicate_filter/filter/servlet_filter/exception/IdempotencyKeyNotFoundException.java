package com.duplicate_filter.filter.servlet_filter.exception;

public class IdempotencyKeyNotFoundException extends Exception {
    
    public IdempotencyKeyNotFoundException(){
        super();
    }

    public IdempotencyKeyNotFoundException(String message){
        super(message);
    }

    public IdempotencyKeyNotFoundException(String message, Throwable cause){
        super(message, cause);
    }

    public IdempotencyKeyNotFoundException(Throwable cause){
        super(cause);
    }


    
}
