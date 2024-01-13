package com.example.userservices.exception;

public class UserServicesException extends RuntimeException{
    public UserServicesException() {
    }

    public UserServicesException(String message) {
        super(message);
    }

    public UserServicesException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserServicesException(Throwable cause) {
        super(cause);
    }

    public UserServicesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
