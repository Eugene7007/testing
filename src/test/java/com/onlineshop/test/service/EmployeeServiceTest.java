package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    EmployeeService employeeService;

    @Test
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        long employeeId = 1L;
        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(
            employeeId,
            "yuriy",
            "software engineer",
            12345L,
            "IT",
            "Manager"
        );

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(employeeEntity));

        when(employeeMapper.toResponse(employeeEntity))
            .thenReturn(employeeResponse);

        var result = employeeService.getEmployeeById(employeeId);

        assertNotNull(result);
        assertEquals(employeeResponse, result);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
       }

    @Test
    void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesntExist() {
        long employeeId = 1L;
        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.getEmployeeById(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void createEmployee_ShouldReturnEmployeeResponse_WhenEmployeeCreated() {
        var employeeRequest = new EmployeeRequest();
        employeeRequest.setName("Yuriy");
        employeeRequest.setPosition("Software Engineer");
        employeeRequest.setSalary(120000L);
        employeeRequest.setDepartmentId(123L);
        employeeRequest.setManagerId(22L);

        var employeeEntity = new Employee();

        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employeeEntity);
        when(employeeRepository.save(employeeEntity)).thenReturn(employeeEntity);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(
            new EmployeeResponse(1L, "Yuriy", "Software Engineer", 120000L, "IT", "Manager")
        );

        var result = employeeService.createEmployee(employeeRequest);

        assertNotNull(result);
        assertEquals(employeeRequest.getName(), result.name());

        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verify(employeeRepository, times(1)).save(employeeEntity);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    void getAllEmployees_ShouldReturnListOfEmployeeResponses() {
        var employee1 = new Employee();
        var employee2 = new Employee();

        var response1 = new EmployeeResponse(1L, "Yuriy", "Engineer", 400L, "IT", "amal");
        var response2 = new EmployeeResponse(2L, "Anna", "Data Analyst", 500L, "analytics", "madina");

        when(employeeRepository.findAll()).thenReturn(List.of(employee1, employee2));
        when(employeeMapper.toResponse(employee1)).thenReturn(response1);
        when(employeeMapper.toResponse(employee2)).thenReturn(response2);

        var result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(List.of(response1, response2), result);

        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(employee1);
        verify(employeeMapper, times(1)).toResponse(employee2);
    }

    @Test
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeeFound() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        var result = employeeService.getAllEmployees();

        assertTrue(result.isEmpty());

        verify(employeeRepository, times(1)).findAll();
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void deleteEmployee_ShouldDeleteEmployee_WhenEmployeeExists() {
        long employeeId = 1L;
        var existingEmployee = new Employee();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    void deleteEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.deleteEmployee(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    void updateEmployee_ShouldUpdateFields_WhenEmployeeExists() {
        long employeeId = 1L;

        var existingEmployee = new Employee();
        existingEmployee.setName("Old Name");
        existingEmployee.setPosition("Old Position");
        existingEmployee.setSalary(50000L);

        var request = new EmployeeRequest();
        request.setName("New Name");
        request.setPosition("New Position");
        request.setSalary(100000L);
        request.setDepartmentId(10L);
        request.setManagerId(2L);

        var response = new EmployeeResponse(
            employeeId, "New Name", "New Position", 100000L, "IT", "Manager"
        );

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);
        when(employeeMapper.toResponse(existingEmployee)).thenReturn(response);

        var result = employeeService.updateEmployee(employeeId, request);

        assertEquals("New Name", existingEmployee.getName());
        assertEquals("New Position", existingEmployee.getPosition());
        assertEquals(100000L, existingEmployee.getSalary());
        assertEquals(response, result);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(existingEmployee);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);
    }

    @Test
    void updateEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        long employeeId = 1L;

        var request = new EmployeeRequest();
        request.setName("New Name");
        request.setPosition("New Position");
        request.setSalary(100000L);
        request.setDepartmentId(10L);
        request.setManagerId(2L);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.updateEmployee(employeeId, request));

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).save(any());
        verifyNoInteractions(employeeMapper);
    }

}