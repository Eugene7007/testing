package com.onlineshop.test.dto.response;

public record ProductResponse(
        Long id,
        String name,
        Long price
) {}