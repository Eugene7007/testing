package com.onlineshop.test.dto.response;

public record CustomerResponse(
        Long id,
        String name,
        String city
) {}