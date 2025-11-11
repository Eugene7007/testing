package com.onlineshop.test.dto.response;

public record EmployeeResponse(
        Long id,
        String name,
        String position,
        Long salary,
        String departmentName,
        String managerName
) {}