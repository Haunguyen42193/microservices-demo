package com.example.userservices.exception;

public class UserNotFoundException extends UserServicesException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
