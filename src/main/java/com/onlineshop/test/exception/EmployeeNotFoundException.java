package com.onlineshop.test.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("Employee с id " + id + " не найден");
    }
}
