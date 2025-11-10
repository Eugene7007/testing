package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeRequest employeeRequest;
    private EmployeeResponse employeeResponse;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("Muhammadqodir Shaymardonov");
        employee.setPosition("Developer");
        employee.setSalary(BigDecimal.valueOf(50000));

        employeeRequest = new EmployeeRequest();
        employeeRequest.setName("Muhammadqodir Shaymardonov");
        employeeRequest.setPosition("Developer");
        employeeRequest.setSalary(BigDecimal.valueOf(50000));

        employeeResponse = new EmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setName("Muhammadqodir Shaymardonov");
        employeeResponse.setPosition("Developer");
        employeeResponse.setSalary(BigDecimal.valueOf(50000));
    }

    @Test
    void getAllEmployees_shouldReturnListOfEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(employee);

        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Muhammadqodir Shaymardonov", result.get(0).getName());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void getAllEmployees_shouldReturnEmptyList() {
        when(employeeRepository.findAll()).thenReturn(new ArrayList<>());

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Muhammadqodir Shaymardonov", result.getName());
    }

    @Test
    void getEmployeeById_shouldThrowExceptionWhenNotFound() {

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    void createEmployee_shouldSaveAndReturnEmployee() {
        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);
        when(employeeRepository.save(employee)).thenReturn(employee);

        EmployeeResponse result = employeeService.createEmployee(employeeRequest);

        assertNotNull(result);
        assertEquals("Muhammadqodir Shaymardonov", result.getName());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void createEmployee_shouldCallMapperToEntity() {

        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        employeeService.createEmployee(employeeRequest);

        verify(employeeMapper, times(1)).toEntity(employeeRequest);
    }

    @Test
    void updateEmployee_shouldUpdateAndReturnEmployee() {
        EmployeeRequest updateRequest = new EmployeeRequest();
        updateRequest.setName("Firdavs Rahmatov");
        updateRequest.setPosition("Senior Developer");
        updateRequest.setSalary(BigDecimal.valueOf(70000));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.updateEmployee(1L, updateRequest);

        assertNotNull(result);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void updateEmployee_shouldThrowExceptionWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.updateEmployee(999L, employeeRequest);
        });
    }

    @Test
    void deleteEmployee_shouldDeleteEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).deleteById(1L);

        employeeService.deleteEmployee(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEmployee_shouldThrowExceptionWhenNotFound() {

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
    }

    @Test
    void getAllEmployees_shouldMapAllEmployeesCorrectly() {

        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setName("Firdavs Rahmatov");

        List<Employee> employees = List.of(employee, employee2);

        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

        List<EmployeeResponse> result = employeeService.getAllEmployees();
        assertEquals(2, result.size());
        verify(employeeMapper, times(2)).toResponse(any(Employee.class));
    }

    @Test
    void updateEmployee_shouldUpdateAllFields() {
        EmployeeRequest updateRequest = new EmployeeRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setPosition("Manager");
        updateRequest.setSalary(BigDecimal.valueOf(80000));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        employeeService.updateEmployee(1L, updateRequest);

        verify(employeeRepository).save(employee);
        assertEquals("Updated Name", employee.getName());
        assertEquals("Manager", employee.getPosition());
    }
}