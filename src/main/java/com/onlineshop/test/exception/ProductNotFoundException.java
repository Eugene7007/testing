package com.onlineshop.test.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product с id " + id + " не найден");
    }
}
