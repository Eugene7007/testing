package com.onlineshop.test.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EmployeeRequest {

    @NotBlank(message = "Имя сотрудника не может быть пустым")
    private String name;

    @NotBlank(message = "Должность сотрудника не может быть пустой")
    private String position;

    @Positive(message = "Зарплата должна быть больше нуля")
    private Long salary;

    @NotNull(message = "ID департамента не может быть null")
    private Long departmentId;

    private Long managerId; // Может быть null, если у сотрудника нет менеджера
}