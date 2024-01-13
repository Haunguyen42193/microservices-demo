package com.example.productservices.exception;

public class InvalidOrderFormatException extends ProductServiceException {
    public InvalidOrderFormatException(String s) {
        super(s);
    }
}
