package com.example.userservices.exception;

public class ExpiredJwtException extends UserServicesException {
    public ExpiredJwtException(String message) {
        super(message);
    }
}
