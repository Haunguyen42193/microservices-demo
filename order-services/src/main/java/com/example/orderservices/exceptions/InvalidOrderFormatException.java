package com.example.orderservices.exceptions;

public class InvalidOrderFormatException extends OrderServiceException {
    public InvalidOrderFormatException(String s) {
        super(s);
    }
}
