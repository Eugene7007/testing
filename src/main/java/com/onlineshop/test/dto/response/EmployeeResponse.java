package com.onlineshop.test.dto.response;

public record EmployeeResponse(
        Long id,
        String name,
        String email,
        String position,
        Long salary,
        String departmentName,
        String managerName
) {}