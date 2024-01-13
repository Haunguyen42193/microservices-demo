package com.example.productservices.exception;

public class ProductServiceException extends RuntimeException{
    public ProductServiceException() {
    }

    public ProductServiceException(String message) {
        super(message);
    }

    public ProductServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductServiceException(Throwable cause) {
        super(cause);
    }

    public ProductServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
