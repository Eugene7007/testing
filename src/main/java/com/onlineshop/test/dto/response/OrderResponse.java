package com.onlineshop.test.dto.response;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long amount,
        String customerName,
        String employeeName,
        LocalDateTime createdAt
) {}
