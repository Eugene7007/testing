package com.onlineshop.test.dto.response;

public record EmployeeResponse(
        Long id,
        String name,
        String position,
        java.math.BigDecimal salary,
        String departmentName,
        String managerName
) {}