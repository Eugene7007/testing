package com.onlineshop.test.controller;

import com.onlineshop.test.dto.request.DepartmentRequest;
import com.onlineshop.test.dto.response.DepartmentResponse;
import com.onlineshop.test.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/departments")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DepartmentController {

    DepartmentService departmentService;

    // Получение всех департаментов
    @GetMapping
    public List<DepartmentResponse> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    // Получение департамента по ID
    @GetMapping("/{id}")
    public DepartmentResponse getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    // Создание нового департамента
    @PostMapping
    public DepartmentResponse createDepartment(@RequestBody @Valid DepartmentRequest request) {
        return departmentService.createDepartment(request);
    }

    // Обновление департамента
    @PutMapping("/{id}")
    public DepartmentResponse updateDepartment(@PathVariable Long id, @RequestBody @Valid DepartmentRequest request) {
        return departmentService.updateDepartment(id, request);
    }

    // Удаление департамента
    @DeleteMapping("/{id}")
    public void deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
    }
}