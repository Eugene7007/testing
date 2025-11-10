package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    Employee employee = new Employee();
    EmployeeResponse employeeResponse;
    EmployeeRequest employeeRequest = new EmployeeRequest();

    @Spy
    EmployeeMapper employeeMapper;

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    @BeforeEach
    void setEmployeeParams() {
        employee.setId(0L);
        employee.setName("Sasha");
        employee.setPosition("Junior Java Developer");
        employee.setSalary(1000L);

        employeeResponse = new EmployeeResponse(0L, "Sasha", "Junior Java Developer", 1000L, "Dev Team", "Eugene");

        employeeRequest.setName("Sasha");
        employeeRequest.setPosition("Junior Java Developer");
        employeeRequest.setSalary(1000L);
        employeeRequest.setDepartmentId(0L);
        employeeRequest.setManagerId(0L);
    }

    @Test
    @DisplayName("Check added employee")
    public void checkFirstEmployee() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result)
            .isNotNull()
            .hasSize(1)
            .first()
            .extracting(EmployeeResponse::name)
            .isEqualTo("Sasha");

        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(employee);
    }

    @Test
    @DisplayName("Check employee by id")
    public void returnEmployeeById() {
        when(employeeRepository.findById(0L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        List<EmployeeResponse> result = Collections.singletonList(employeeService.getEmployeeById(0L));

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(EmployeeResponse::name)
                .isEqualTo("Sasha");

        verify(employeeRepository, times(1)).findById(0L);
        verify(employeeMapper, times(1)).toResponse(employee);
    }

    @Test
    @DisplayName("Check throw EmployeeNotFoundException")
    void checkEmployeeNotFound_Exception() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(1L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("1");

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Create new employee successfully")
    void createEmployee() {
        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.createEmployee(employeeRequest);

        assertThat(result)
                .isNotNull()
                .extracting(EmployeeResponse::name)
                .isEqualTo("Sasha");

        verify(employeeMapper).toEntity(employeeRequest);
        verify(employeeRepository).save(employee);
        verify(employeeMapper).toResponse(employee);
    }

    @Test
    @DisplayName("Update existing employee")
    void updateEmployee() {
        when(employeeRepository.findById(0L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.updateEmployee(0L, employeeRequest);

        assertThat(result)
                .isNotNull()
                .extracting(EmployeeResponse::name)
                .isEqualTo("Sasha");

        verify(employeeRepository).findById(0L);
        verify(employeeRepository).save(employee);
        verify(employeeMapper).toResponse(employee);
    }

    @Test
    @DisplayName("Check throw exception when updating non exist employee")
    void checkThrowExceptionUpdateNonExistEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, employeeRequest))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("1");

        verify(employeeRepository).findById(1L);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete employee successfully when found")
    void deleteEmployee() {
        when(employeeRepository.findById(0L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(0L);

        verify(employeeRepository).findById(0L);
        verify(employeeRepository).deleteById(0L);
    }

    @Test
    @DisplayName("Check throw exception when deleting non exist employee")
    void checkExceptionWhenDeletingNonExistEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(1L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("1");

        verify(employeeRepository).findById(1L);
        verify(employeeRepository, never()).deleteById(anyLong());
    }
    
    @Test
    @DisplayName("Map list of employees correctly in getAllEmployees()")
    void mapListEmployee() {
        var employee2 = new Employee(2L, "John", "java dev", 100L, null, null);
        var response2 = new EmployeeResponse(2L, "John", "java dev", 100L, "Dev team", "Eugene");

        when(employeeRepository.findAll()).thenReturn(List.of(employee, employee2));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);
        when(employeeMapper.toResponse(employee2)).thenReturn(response2);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result)
                .hasSize(2)
                .extracting(EmployeeResponse::name)
                .containsExactly("Sasha", "John");

        verify(employeeRepository).findAll();
        verify(employeeMapper, times(2)).toResponse(any());
    }
    
    @Test
    @DisplayName("Save() exactly once when creating employee")
    void shouldCallSaveOnceWhenCreatingEmployee() {
        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        employeeService.createEmployee(employeeRequest);

        verify(employeeRepository, times(1)).save(employee);
        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verify(employeeMapper, times(1)).toResponse(employee);
    }
}