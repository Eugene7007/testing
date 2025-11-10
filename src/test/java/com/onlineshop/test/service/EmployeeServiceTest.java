package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// Unit tests
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Spy
    EmployeeMapper employeeMapper;
    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    @Test
    @DisplayName("getById")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        var employeeId = 1L;
        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(
                1L,
                "John Doe",
                "accountant",
                3000L,
                "finance",
                "Mark Avrelian");

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employeeEntity));

        when(employeeMapper.toResponse(employeeEntity))
                .thenReturn(employeeResponse);

        var result = employeeService.getEmployeeById(employeeId);

        assertThat(result).isNotNull();
        assertThat(employeeResponse)
                .isNotNull()
                .isEqualTo(result);

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("getAllEmployees")
    void getAllEmployees_ShouldReturnEmployeesList_WhenEmployeesExists() {
        var employeeEntityOne = new Employee();
        var employeeEntityTwo = new Employee();
        var employeeEntityList = List.of(employeeEntityOne, employeeEntityTwo);
        var employeeResponseOne = new EmployeeResponse(1L, "John Doe", "accountant", 3000L, "Finance", "Mark Aurelian");
        var employeeResponseTwo = new EmployeeResponse(2L, "Sam Serious", "analyst", 3200L, "Production", "Mark Aurelian");

        when(employeeRepository.findAll())
                .thenReturn(employeeEntityList);

        when(employeeMapper.toResponse(employeeEntityOne))
                .thenReturn(employeeResponseOne);
        when(employeeMapper.toResponse(employeeEntityTwo))
                .thenReturn(employeeResponseTwo);

        var result = employeeService.getAllEmployees();

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactly(employeeResponseOne, employeeResponseTwo);

        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(employeeEntityOne);
        verify(employeeMapper, times(1)).toResponse(employeeEntityTwo);
    }

    @Test
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExist() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        var result = employeeService.getAllEmployees();

        assertThat(result)
                .isNotNull()
                .isEmpty();

        verify(employeeRepository, times(1)).findAll();
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void getAllEmployees_ShouldSkipNullResponses_WhenMapperReturnsNull() {
        var employeeEntity = new Employee();
        when(employeeRepository.findAll()).thenReturn(List.of(employeeEntity));
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(null);

        var result = employeeService.getAllEmployees();

        assertThat(result).isNotNull().isEmpty();

        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    void getAllEmployees_ShouldThrowException_WhenRepositoryThrows() {
        when(employeeRepository.findAll()).thenThrow(new RuntimeException("connection was broken"));

        assertThatThrownBy(() -> employeeService.getAllEmployees())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("connection was broken");

        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("createEmployee_ShouldReturnResponse_WhenEmployeeIsCreated")
    void createEmployee_ShouldReturnResponse_WhenEmployeeIsCreated() {

        var employeeRequest = new EmployeeRequest();
        employeeRequest.setName("John Doe");
        employeeRequest.setPosition("accountant");
        employeeRequest.setSalary(3000L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(1L);

        var department = new Department();
        department.setId(10L);
        department.setName("Finance");

        var employeeEntity = new Employee(1L, "John Doe", "accountant", 3000L, department, null);

        var employeeResponse = new EmployeeResponse(1L, "John Doe", "accountant", 3000L, "Finance", "Нет менеджера");

        when(employeeMapper.toEntity(employeeRequest))
                .thenReturn(employeeEntity);

        when(employeeRepository.save(employeeEntity))
                .thenReturn(employeeEntity);

        when(employeeMapper.toResponse(employeeEntity))
                .thenReturn(employeeResponse);

        var result = employeeService.createEmployee(employeeRequest);

        assertThat(result)
                .isNotNull()
                .isEqualTo(employeeResponse);

        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verify(employeeRepository, times(1)).save(employeeEntity);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    @DisplayName("createEmployee_ShouldThrowException_WhenRepositoryFails")
    void createEmployee_ShouldThrowException_WhenRepositoryFails() {

        var employeeRequest = new EmployeeRequest();
        employeeRequest.setName("John Doe");
        employeeRequest.setPosition("accountant");
        employeeRequest.setSalary(3000L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(1L);

        var employeeEntity = new Employee();

        when(employeeMapper.toEntity(employeeRequest))
                .thenReturn(employeeEntity);

        when(employeeRepository.save(employeeEntity))
                .thenThrow(new RuntimeException("connection was broken"));

        assertThatThrownBy(() -> employeeService.createEmployee(employeeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("connection was broken");

        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verify(employeeRepository, times(1)).save(employeeEntity);
        verify(employeeMapper, never()).toResponse(any());
    }

    @Test
    void deleteEmployee_shouldDelete_whenEmployeeExists() {
        Long employeeId = 1L;
        Employee employee = new Employee();
        employee.setId(employeeId);

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).deleteById(employeeId);
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    void deleteEmployee_ShouldThrowException_WhenEmployeeNotFound() {
        Long employeeId = 404L;

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(employeeId))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining(String.valueOf("id " + employeeId + " "));

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).deleteById(anyLong());
    }
}