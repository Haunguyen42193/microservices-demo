package com.example.productservices.exception;

public class ProductNotFoundException extends ProductServiceException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}

