package com.onlineshop.test.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderRequest {

    @Positive(message = "Сумма заказа должна быть положительным числом")
    @NotNull(message = "Сумма заказа не может быть null")
    private Long amount;

    @NotNull(message = "ID клиента не может быть null")
    private Long customerId;

    @NotNull(message = "ID сотрудника не может быть null")
    private Long employeeId;

    @NotNull(message = "Дата создания не может быть null")
    private LocalDateTime updatedAt;
}