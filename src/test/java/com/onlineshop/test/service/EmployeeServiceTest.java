package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Spy
    EmployeeMapper employeeMapper;
    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    ArgumentCaptor<Employee> employeeCaptor;

    @Test
    @DisplayName("getEmployeeById should return employee response when employee exists")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        var employeeId = 1L;

        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(1L, "Anthony Stark",
            "Engineer", 10_000_000L, "Computer technologies manufacturing",
            "Howard Stark");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employeeEntity));
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        var result = employeeService.getEmployeeById(employeeId);

        assertThat(result)
            .isNotNull();

        assertThat(result)
            .isNotNull()
            .isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    @DisplayName("getEmployeeById should throw exception when employee doesn't exist")
    void getEmployeeById_ShouldThrowException_WhenEmployeeDoesNotExist() {
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));
        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("getAllEmployees should return list of EmployeeResponse when employees exist")
    void getAllEmployees_ShouldReturnEmployeeResponse_WhenEmployeeExists(){
        var employeeEntity1 = new Employee();
        var employeeEntity2 = new Employee();

        var employees = List.of(employeeEntity1, employeeEntity2);

        var employeeResponse1 = new EmployeeResponse(1L, "Anthony Stark", "Engineer",
            10_000_000L, "Technology", "Howard Stark");
        var employeeResponse2 = new EmployeeResponse(2L, "Steve Rogers","Captain",5_000_000L,
            "Security", "Peggy Carter");

        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.toResponse(employeeEntity1)).thenReturn(employeeResponse1);
        when(employeeMapper.toResponse(employeeEntity2)).thenReturn(employeeResponse2);

        var result = employeeService.getAllEmployees();

        assertThat(result)
            .isNotNull()
            .hasSize(employees.size())
            .containsExactly(employeeResponse1, employeeResponse2);

        //to check whether these verifications were called in this way or n times
        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(employeeEntity1);
        verify(employeeMapper, times(1)).toResponse(employeeEntity2);
        verifyNoMoreInteractions(employeeRepository, employeeMapper);
    }

    @Test
    @DisplayName("getAllEmployees should return empty list when no employees exist")
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
    @DisplayName("createEmployee should create employee when employee does not exist")
    void createEmployee_ShouldCreateEmployee_WhenEmployeeDoesNotExist(){
        var employeeRequest = new EmployeeRequest();
        employeeRequest.setSalary(13_000_000L);
        employeeRequest.setName("Bruce");
        employeeRequest.setPosition("Doctor");
        employeeRequest.setManagerId(1L);
        employeeRequest.setDepartmentId(1L);

        var employeeEntity = new Employee();

        var employeeResponse = new EmployeeResponse(3L, "Bruce", "Doctor",
            13_000_000L, "Future tech", "Bruuuuce");

        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employeeEntity);
        when(employeeRepository.save(employeeEntity)).thenReturn(employeeEntity);
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        var result = employeeService.createEmployee(employeeRequest);

        assertThat(result)
            .isNotNull()
                .isEqualTo(employeeResponse);

        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verify(employeeRepository, times(1)).save(employeeEntity);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    @DisplayName("updateEmployee should throw exception where employee does not exist")
    void updateEmployee_ShouldThrowException_WhenEmployeeDoesNotExist(){
        var employeeId = 1L;

        var employeeRequest = new EmployeeRequest();
        employeeRequest.setSalary(13_000_000L);
        employeeRequest.setName("Bruce");
        employeeRequest.setPosition("Doctor");
        employeeRequest.setManagerId(1L);
        employeeRequest.setDepartmentId(1L);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () ->
            employeeService.updateEmployee(employeeId, employeeRequest));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("updateEmployee should update employee when employee exists")
    void updateEmployee_ShouldUpdateEmployee_WhenEmployeeExists(){
        var employeeId = 1L;

        var employeeRequest = new EmployeeRequest();
        employeeRequest.setSalary(13_000_000L);
        employeeRequest.setName("Bruce Banner");
        employeeRequest.setPosition("Doctor");
        employeeRequest.setManagerId(1L);
        employeeRequest.setDepartmentId(1L);

        var employeeEntity = new Employee(); //existing

        var employeeResponse = new EmployeeResponse(3L, "Bruce", "Doctor",
            13_000_000L, "Future tech", "Bruuuuce");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employeeEntity));
        when(employeeRepository.save(employeeEntity)).thenReturn(employeeEntity);
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        var result = employeeService.updateEmployee(employeeId, employeeRequest);

        assertThat(result)
            .isNotNull()
            .isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(employeeEntity);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    @DisplayName("deleteEmployee should delete employee when employee exists")
    void deleteEmployee_ShouldDeleteEmployee_WhenEmployeeExists(){
        var employeeId = 1L;

        var employee = new Employee();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    @DisplayName("deleteEmployee should throw exception when employee does not exist")
    void deleteEmployee_ShouldThrowException_WhenEmployeeDoesNotExist() {
        var employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).deleteById(any()); // should not be called
        verifyNoMoreInteractions(employeeRepository);
    }
}