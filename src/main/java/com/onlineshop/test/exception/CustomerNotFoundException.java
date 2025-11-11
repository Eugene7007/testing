package com.onlineshop.test.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer с id " + id + " не найден");
    }
}
