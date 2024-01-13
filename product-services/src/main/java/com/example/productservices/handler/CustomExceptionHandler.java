package com.example.productservices.handler;

import com.example.productservices.exception.OrderNotFoundException;
import com.example.productservices.exception.ProductNotFoundException;
import com.example.productservices.exception.WriteJsonAsStringException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handlerProductNotFound(ProductNotFoundException ex, WebRequest webRequest) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Object> handlerOrderNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(WriteJsonAsStringException.class)
    public ResponseEntity<Object> handlerWriteJsonAsString(WriteJsonAsStringException ex) {
        return ResponseEntity.status(500).body(ex.getMessage());
    }
}
