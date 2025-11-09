package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    void setEmployeeService() {
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
    @DisplayName("This case should as")
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

}