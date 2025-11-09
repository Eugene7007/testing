package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.DuplicateEmployeeException;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Spy
    EmployeeMapper employeeMapper;
    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    @Captor
    ArgumentCaptor<Employee> employeeCaptor;

    @Test
    @DisplayName("Return employee responses when employeeRepository is not empty")
    void getAllEmployees_ShouldReturnEmployeeResponses_WhenEmployeeRepositoryIsNotEmpty() {
        //Arrange
        var department = new Department(1L,"Marketing", "Tashkent");
        var manager = new Employee(1L, "Tom", "tom@mail.ru", "Manager", 1000L, department, null);
        var e1 = new Employee(2L,"Alice", "alisa@mail.ru","Specialist",500L, department, manager);
        var e2 = new Employee(3L,"Bob", "bob@mail.ru","Specialist",600L, department, manager);

        var employees = List.of(e1, e2);

        var response1 = new EmployeeResponse(2L, "Alice", "alisa@mail.ru","Specialist", 500L, "Marketing", "Tom");
        var response2 = new EmployeeResponse(2L, "Bob", "bob@mail.ru","Specialist", 600L, "Marketing", "Tom");

        var responseList = List.of(response1, response2);

        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.toResponse(e1)).thenReturn(response1);
        when(employeeMapper.toResponse(e2)).thenReturn(response2);

        //Act
        var result = employeeService.getAllEmployees();

        //Assert
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactly(response1, response2);

        // Verify
        verify(employeeRepository).findAll();
        verify(employeeMapper).toResponse(e1);
        verify(employeeMapper).toResponse(e2);
        verify(employeeMapper, times(2)).toResponse(employeeCaptor.capture());
    }

    @Test
    @DisplayName("Return empty list when employeeRepository is empty")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExists() {
        //Arrange
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        //Act
        var result = employeeService.getAllEmployees();

        //Assert
        assertThat(result)
                .isNotNull()
                .isEmpty();

        // Verify
        verify(employeeRepository).findAll();
        verify(employeeMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Return employeeResponse when employee is exists")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        //Arrange
        var employeeId = 1L;
        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(1L, "Bob", "bob@mail.ru","Specialist", 1000L, "Marketing", "Tom");

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employeeEntity));
        when(employeeMapper.toResponse(employeeEntity))
                .thenReturn(employeeResponse);

        //Act
        var result = employeeService.getEmployeeById(employeeId);

        //Assert
        assertThat(result).isNotNull();
        assertThat(employeeResponse)
                .isNotNull()
                .isEqualTo(result);

        //Verify
        verify(employeeRepository).findById(employeeId);
        verify(employeeMapper).toResponse(employeeEntity);
        verify(employeeMapper).toResponse(employeeCaptor.capture());
    }

    @Test
    @DisplayName("Throw employeeNotFoundException when employee is not exist")
    void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeNotExist() {
        //Arrange
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        //Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));

        //Verify
        verify(employeeRepository).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("Should save employee and return employeeResponse")
    void createEmployee_ShouldSaveEmployeeEntityAndReturnEmployeeResponse_WhenEmployeeNotExist() {
        //Arrange
        var employeeEntity = new Employee();
        var employeeRequest = new EmployeeRequest();
        employeeRequest.setName("Bob");
        employeeRequest.setEmail("bob@mail.ru");
        employeeRequest.setPosition("Specialist");
        employeeRequest.setSalary(100L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(2L);
        var employeeResponse = new EmployeeResponse(1L,"Bob","bob@mail.ru","Specialist",100L,"Marketing","Alice");

        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employeeEntity);
        when(employeeRepository.existsByEmail(employeeRequest.getEmail())).thenReturn(false);
        when(employeeRepository.save(employeeEntity)).thenReturn(employeeEntity);
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        //Act
        var result = employeeService.createEmployee(employeeRequest);

        //Assert
        assertEquals(result, employeeResponse);

        //Verify
        verify(employeeMapper).toEntity(employeeRequest);
        verify(employeeRepository).existsByEmail(employeeRequest.getEmail());
        verify(employeeRepository).save(employeeEntity);
        verify(employeeMapper).toResponse(employeeEntity);
    }

    @Test
    @DisplayName("Should throw DuplicateEmployeeException when employee exists")
    void createEmployee_ShouldThrowDuplicateEmployeeException_WhenEmployeeExists() {
        //Arrange
        var employeeEntity = new Employee();
        var employeeRequest = new EmployeeRequest();
        employeeRequest.setEmail("bob@mail.ru");

        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employeeEntity);
        when(employeeRepository.existsByEmail(employeeRequest.getEmail())).thenReturn(true);

        //Act + assert
        assertThrows(DuplicateEmployeeException.class, () -> employeeService.createEmployee(employeeRequest));

        //Verify
        verify(employeeMapper).toEntity(employeeRequest);
        verify(employeeRepository).existsByEmail(employeeRequest.getEmail());
        verify(employeeRepository, never()).save(any());
        verify(employeeMapper, never()).toResponse(any());
    }
}
