package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.DepartmentRequest;
import com.onlineshop.test.dto.response.DepartmentResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.exception.DepartmentNotFoundException;
import com.onlineshop.test.mapper.DepartmentMapper;
import com.onlineshop.test.repository.DepartmentRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DepartmentService {

    DepartmentMapper departmentMapper;
    DepartmentRepository departmentRepository;

    // Получение всех департаментов
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository
                .findAll()
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    // Получение департамента по ID = 10001
    public DepartmentResponse getDepartmentById(Long id) {
        return departmentRepository
                .findById(id)
                .map(departmentMapper::toResponse)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    // Создание нового департамента
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        var department = departmentMapper.toEntity(request);
        departmentRepository.save(department);

        return departmentMapper.toResponse(department);
    }

    // Обновление департамента
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        var existingDepartment = departmentRepository
                .findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        existingDepartment.setName(request.getName());
        existingDepartment.setLocation(request.getLocation());
        departmentRepository.save(existingDepartment);

        return departmentMapper.toResponse(existingDepartment);
    }

    // Удаление департамента
    public void deleteDepartment(Long id) {
        departmentRepository
                .findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        departmentRepository.deleteById(id);
    }
}