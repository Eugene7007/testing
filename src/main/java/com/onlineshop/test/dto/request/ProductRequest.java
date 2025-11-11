package com.onlineshop.test.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "Название продукта не может быть пустым")
    private String name;

    @Positive(message = "Цена продукта должна быть больше нуля")
    private Long price;
}