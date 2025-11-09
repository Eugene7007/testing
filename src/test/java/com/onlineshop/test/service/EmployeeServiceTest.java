package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Spy
    EmployeeMapper employeeMapper;

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;



    @Test
    void getAllEmployeesTest() {

        var employee = new Employee();
        var employeeResponse= new EmployeeResponse(1l,"Shrek","zet",212L,"department","manager");


        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var result = employeeService.getAllEmployees();

        assertThat(result).isNotNull().containsExactly(employeeResponse);


        verify(employeeRepository).findAll();
        verify(employeeMapper).toResponse(employee);
    }


    @Test
    void getEmployeeByIdTest() {
        var employee = new Employee();
        var employeeResponse= new EmployeeResponse(1l,"Shrek","zet",212L,"department","manager");

        when(employeeRepository.findById(1l))
                .thenReturn(Optional.of(employee));

        when(employeeMapper.toResponse(employee))
                .thenReturn(employeeResponse);

        var result = employeeService.getEmployeeById(1l);


        assertThat(result).isNotNull().isEqualTo(employeeResponse);
        assertThat(employeeResponse).isNotNull().isEqualTo(result);

        verify(employeeRepository,times(1)).findById(1l);
        verify(employeeMapper,times(1)).toResponse(employee);

    }


    @Test
    void createEmployeeTest() {
        var employeeRequest = new EmployeeRequest("Shrek","zet",212L,1l,null);
        var employeeResponse = new EmployeeResponse(1l,"Shrek","zet",212L,"department","manager");
        var employee = new Employee();

        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);


        var result = employeeService.createEmployee(employeeRequest);

        assertEquals(employeeResponse,result);

        verify(employeeRepository,times(1)).save(employee);
        verify(employeeMapper,times(1)).toEntity(employeeRequest);
        verify(employeeMapper,times(1)).toResponse(employee);
    }


    @Test
    void updateEmployeeTest() {
        var employeeRequest = new EmployeeRequest("Shrek","zet",212L,1l,null);
        var employeeResponse = new EmployeeResponse(1l,"Shrek","zet",212L,"department","manager");
        var employee = new Employee();

        when(employeeRepository.findById(1l)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var result = employeeService.updateEmployee(1l,employeeRequest);
        assertEquals(employeeResponse,result);

        verify(employeeRepository,times(1)).findById(1l);
        verify(employeeRepository,times(1)).save(employee);
        verify(employeeMapper,times(1)).toResponse(employee);

    }


    @Test
    void deleteEmployeeTest() {
        var employee = new Employee();

        employee.setId(1l);
        employeeRepository.save(employee);
        employeeRepository.deleteById(1l);

        assertFalse(employeeRepository.findById(1l).isPresent());


    }
}
