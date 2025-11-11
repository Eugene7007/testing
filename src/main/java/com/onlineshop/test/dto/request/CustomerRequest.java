package com.onlineshop.test.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequest {

    @NotBlank(message = "Имя клиента не может быть пустым")
    private String name;

    @NotBlank(message = "Город клиента не может быть пустым")
    private String city;
}