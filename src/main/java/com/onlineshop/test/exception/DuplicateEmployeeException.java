package com.onlineshop.test.exception;

public class DuplicateEmployeeException extends RuntimeException {
    public DuplicateEmployeeException(String email) {
        super("Employee с " + email + " уже существует!");
    }
}
