package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

    Employee employee;
    EmployeeResponse employeeResponse;
    EmployeeRequest employeeRequest;
    Department department;

    // Этот метод setUp() выполняется перед каждым тестом для подготовки данных
    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Department 1");
        department.setLocation("Tashkent");

        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setPosition("Developer");
        employee.setSalary(5000L);
        employee.setDepartment(department);
        employee.setManager(null);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setName("Alice Doe");
        manager.setPosition("Developer");
        manager.setSalary(5000L);
        manager.setDepartment(department);
        manager.setManager(null);

        employee.setManager(manager);

        employeeResponse = new EmployeeResponse(
                1L,
                "John Doe",
                "Developer",
                5000L,
                department.getName(),
                "Alice Doe"
        );

        employeeRequest = new EmployeeRequest();
        employeeRequest.setName("John Doe");
        employeeRequest.setPosition("Developer");
        employeeRequest.setSalary(5000L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(2L);
    }

    @Test
    @DisplayName("getEmployeeById - when employee exists")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getEmployeeById(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeMapper, times(1)).toResponse(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("getEmployeeById - when employee does not exist")
    void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(1L));

        verify(employeeRepository, times(1)).findById(1L);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("createEmployee - should save employee and return response")
    void createEmployee_ShouldReturnEmployeeResponse() {
        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.createEmployee(employeeRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).save(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue().getName()).isEqualTo("John Doe");
        verify(employeeMapper, times(1)).toResponse(employee);
    }

    @Test
    @DisplayName("getAllEmployees - should return list of employee responses")
    void getAllEmployees_ShouldReturnListOfEmployeeResponses() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(employeeCaptor.capture());
    }

    @Test
    @DisplayName("deleteEmployee - should throw exception if employee not found")
    void deleteEmployee_ShouldThrowException_WhenEmployeeNotFound() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(1L));

        verify(employeeRepository, times(1)).findById(1L);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("updateEmployee - should update employee and return response")
    void updateEmployee_ShouldReturnUpdatedEmployeeResponse_WhenEmployeeExists() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.updateEmployee(1L, employeeRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(employeeCaptor.capture());
        Employee captured = employeeCaptor.getValue();
        assertThat(captured.getName()).isEqualTo(employeeRequest.getName());
        assertThat(captured.getPosition()).isEqualTo(employeeRequest.getPosition());
        assertThat(captured.getSalary()).isEqualTo(employeeRequest.getSalary());
        assertThat(captured.getDepartment()).isEqualTo(employee.getDepartment());
        assertThat(captured.getManager()).isEqualTo(employee.getManager());

        verify(employeeMapper, times(1)).toResponse(employee);
    }

}
