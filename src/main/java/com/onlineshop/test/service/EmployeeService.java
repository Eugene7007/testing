package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EmployeeService {

    EmployeeMapper employeeMapper;
    EmployeeRepository employeeRepository;

    // Получение всех сотрудников
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository
                .findAll()
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    // Получение сотрудника по ID
    public EmployeeResponse getEmployeeById(Long id) {
        return employeeRepository
                .findById(id)
                .map(employeeMapper::toResponse)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    // Создание нового сотрудника
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        var employee = employeeMapper.toEntity(request);
        employeeRepository.save(employee);

        return employeeMapper.toResponse(employee);
    }

    // Обновление сотрудника
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        var existingEmployee = employeeRepository
                .findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        existingEmployee.setName(request.getName());
        existingEmployee.setPosition(request.getPosition());
        existingEmployee.setSalary(request.getSalary());
        existingEmployee.setDepartment(existingEmployee.getDepartment());
        existingEmployee.setManager(existingEmployee.getManager());
        employeeRepository.save(existingEmployee);

        return employeeMapper.toResponse(existingEmployee);
    }

    // Удаление сотрудника
    public void deleteEmployee(Long id) {
        employeeRepository
                .findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        employeeRepository.deleteById(id);
    }
}