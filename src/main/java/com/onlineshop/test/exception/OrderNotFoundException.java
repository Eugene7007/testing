package com.onlineshop.test.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Order с id " + id + " не найден");
    }
}