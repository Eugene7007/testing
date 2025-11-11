package com.onlineshop.test.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Название департамента не может быть пустым")
    private String name;

    @NotBlank(message = "Локация департамента не может быть пустой")
    private String location;
}