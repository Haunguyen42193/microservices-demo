package com.example.userservices.exception;

public class UsernameExistedException extends UserServicesException {
    public UsernameExistedException(String message) {
        super(message);
    }

    public UsernameExistedException() {
    }

    public UsernameExistedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsernameExistedException(Throwable cause) {
        super(cause);
    }

    public UsernameExistedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
