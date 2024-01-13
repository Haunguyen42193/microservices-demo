package com.example.productservices.exception;

public class OrderNotFoundException extends ProductServiceException {
    public OrderNotFoundException(String s) {
        super(s);
    }
}
