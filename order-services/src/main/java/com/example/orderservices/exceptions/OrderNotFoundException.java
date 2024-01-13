package com.example.orderservices.exceptions;

public class OrderNotFoundException extends OrderServiceException {
    public OrderNotFoundException(String s) {
        super(s);
    }
}
