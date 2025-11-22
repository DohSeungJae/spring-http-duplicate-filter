package com.duplicate_filter.filter.servlet_filter.exception;

public class MissingIdempotencyKeyException extends RuntimeException {
    
    public MissingIdempotencyKeyException(){
        super();
    }

    public MissingIdempotencyKeyException(String message){
        super(message);
    }

    public MissingIdempotencyKeyException(String message, Throwable cause){
        super(message, cause);
    }

    public MissingIdempotencyKeyException(Throwable cause){
        super(cause);
    }


    
}
