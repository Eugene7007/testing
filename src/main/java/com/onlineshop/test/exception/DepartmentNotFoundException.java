package com.onlineshop.test.exception;

public class DepartmentNotFoundException extends RuntimeException {

    public DepartmentNotFoundException(Long id) {
        super("Department с id " + id + " не найден");
    }
}
