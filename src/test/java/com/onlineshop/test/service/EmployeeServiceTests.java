package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTests {

    Random random = new SecureRandom();

    @Spy
    EmployeeMapper employeeMapper;
    @Mock
    EmployeeRepository employeeRepository;


    @InjectMocks
    EmployeeService employeeService;

    @Test
    public void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        // Arrange
        long employeeId = 1L;

        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(
                employeeId,
                "Murio",
                "Junior Backend",
                3000L,
                "IT",
                "Evgeniy"
        );

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employeeEntity));

        when(employeeMapper.toResponse(employeeEntity))
                .thenReturn(employeeResponse);

        // Act
        var result = employeeService.getEmployeeById(employeeId);


        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    public void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesntExists() {
        long employeeId = 1L;

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    public void getAllEmployees_ShouldReturnListOfEmployeeResponses_WhenEmployeesExist() {
        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(
                1L,
                "Murio",
                "Junior Backend",
                3000L,
                "IT",
                "Evgeniy"
        );

        when(employeeRepository.findAll()).thenReturn(List.of(employeeEntity));
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        var result = employeeService.getAllEmployees();

        assertThat(result).isNotEmpty();
        assertThat(result).containsExactly(employeeResponse);

        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    public void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExist() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        var result = employeeService.getAllEmployees();

        assertThat(result).isEmpty();

        verify(employeeRepository, times(1)).findAll();
        verifyNoInteractions(employeeMapper);
    }

    @Test
    public void createEmployee_ShouldReturnEmployeeResponse_WhenEmployeeIsCreated() {
        var employeeRequest = new EmployeeRequest();
        employeeRequest.setName("Murio");
        employeeRequest.setPosition("Junior Backend");
        employeeRequest.setSalary(3000L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(1L);

        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(
                1L,
                "Murio",
                "Junior Backend",
                3000L,
                "IT",
                "Evgeniy"
        );

        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employeeEntity);
        when(employeeRepository.save(employeeEntity)).thenReturn(employeeEntity);
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        var result = employeeService.createEmployee(employeeRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verify(employeeRepository, times(1)).save(employeeEntity);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    public void updateEmployee_ShouldReturnUpdatedEmployeeResponse_WhenEmployeeExists() {
        long employeeId = 1L;

        var employeeRequest = new EmployeeRequest();
        employeeRequest.setName("Murio");
        employeeRequest.setPosition("Junior Backend");
        employeeRequest.setSalary(3000L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(1L);

        var existingEmployee = new Employee();
        var updatedEmployeeResponse = new EmployeeResponse(
                employeeId,
                "Muriooo",
                "Middle Backend",
                5000L,
                "IT",
                "Evgeniy"
        );

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);
        when(employeeMapper.toResponse(existingEmployee)).thenReturn(updatedEmployeeResponse);

        var result = employeeService.updateEmployee(employeeId, employeeRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedEmployeeResponse);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(existingEmployee);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);
    }

    @Test
    public void updateEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        long employeeId = 1L;

        var employeeRequest = new EmployeeRequest();
        employeeRequest.setName("Murio");
        employeeRequest.setPosition("Junior Backend");
        employeeRequest.setSalary(3000L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(1L);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(employeeId, employeeRequest));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    public void deleteEmployee_ShouldDeleteEmployee_WhenEmployeeExists() {
        long employeeId = 1L;
        var existingEmployee = new Employee();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    public void deleteEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        long employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(0)).deleteById(employeeId);
    }

    @Test
    public void createEmployee_ShouldThrowException_WhenMapperFails() {
        var employeeRequest = new EmployeeRequest();

        when(employeeMapper.toEntity(employeeRequest)).thenThrow(new RuntimeException("mapper failed"));

        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(employeeRequest));

        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verifyNoInteractions(employeeRepository);
    }
}
