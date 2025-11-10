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

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    @Captor
    private ArgumentCaptor<Employee> employeeCaptor;

    private Employee employee;
    private Employee manager;
    private Department department;
    private EmployeeResponse employeeResponse;
    private EmployeeRequest employeeRequest;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Calipso");
        department.setLocation("Tashkent");

        manager = new Employee();
        manager.setId(2L);
        manager.setName("Davy Jones");
        manager.setPosition("Developer");
        manager.setSalary(5000L);
        manager.setDepartment(department);

        employee = new Employee();
        employee.setId(1L);
        employee.setName("Jack Sparrow");
        employee.setPosition("Developer");
        employee.setSalary(5000L);
        employee.setDepartment(department);
        employee.setManager(manager);

        employeeResponse = new EmployeeResponse(
                1L,
                "Jack Sparrow",
                "Developer",
                5000L,
                department.getName(),
                manager.getName()
        );

        employeeRequest = new EmployeeRequest();
        employeeRequest.setName("Jack Sparrow");
        employeeRequest.setPosition("Developer");
        employeeRequest.setSalary(5000L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(2L);
    }

    @Test
    @DisplayName("getAllEmployees - should return list of employee responses")
    void getAllEmployees_ShouldReturnListOfEmployeeResponses() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(employeeResponse);

        verify(employeeRepository).findAll();
        verify(employeeMapper).toResponse(employee);
    }

    @Test
    @DisplayName("getAllEmployees - should return empty list when no employees")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result).isEmpty();
        verify(employeeRepository).findAll();
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("getEmployeeById - should return employee when exists")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getEmployeeById(1L);

        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeRepository).findById(1L);
        verify(employeeMapper).toResponse(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue().getName()).isEqualTo("Jack Sparrow");
    }

    @Test
    @DisplayName("getEmployeeById - should throw exception when employee does not exist")
    void getEmployeeById_ShouldThrowException_WhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(99L));

        verify(employeeRepository).findById(99L);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("createEmployee - should save employee and return response")
    void createEmployee_ShouldReturnEmployeeResponse() {
        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.createEmployee(employeeRequest);

        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeMapper).toEntity(employeeRequest);
        verify(employeeRepository).save(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue().getName()).isEqualTo("Jack Sparrow");
        verify(employeeMapper).toResponse(employee);
    }

    @Test
    @DisplayName("createEmployee - should throw exception when mapper fails")
    void createEmployee_ShouldThrowException_WhenMapperFails() {
        var req = new EmployeeRequest();
        req.setName("X");
        req.setPosition("Intern");
        req.setSalary(1000L);

        when(employeeMapper.toEntity(req)).thenThrow(new RuntimeException("Mapping failed"));

        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(req));
        verify(employeeMapper).toEntity(req);
        verifyNoInteractions(employeeRepository);
    }

    @Test
    @DisplayName("updateEmployee - should update employee and return response")
    void updateEmployee_ShouldUpdateFieldsAndReturnResponse_WhenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var updatedRequest = new EmployeeRequest();
        updatedRequest.setName("Jack Sparrow");
        updatedRequest.setPosition("Lead");
        updatedRequest.setSalary(6000L);

        EmployeeResponse result = employeeService.updateEmployee(1L, updatedRequest);

        assertThat(employee.getName()).isEqualTo("Jack Sparrow");
        assertThat(employee.getPosition()).isEqualTo("Lead");
        assertThat(employee.getSalary()).isEqualTo(6000L);
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(employeeCaptor.capture());
        verify(employeeMapper).toResponse(employee);
    }

    @Test
    @DisplayName("updateEmployee - should throw exception when employee not found")
    void updateEmployee_ShouldThrowException_WhenEmployeeNotFound() {
        var req = new EmployeeRequest();
        req.setName("Ghost");
        req.setPosition("None");
        req.setSalary(0L);

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(99L, req));
        verify(employeeRepository).findById(99L);
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    @DisplayName("deleteEmployee - should delete when employee exists")
    void deleteEmployee_ShouldDelete_WhenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteEmployee - should throw exception when employee not found")
    void deleteEmployee_ShouldThrowException_WhenEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(99L));

        verify(employeeRepository).findById(99L);
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("createEmployee - should throw exception when salary is negative")
    void createEmployee_ShouldThrowException_WhenSalaryIsNegative() {
        EmployeeRequest req = new EmployeeRequest();
        req.setName("John Doe");
        req.setPosition("Intern");
        req.setSalary(-100L);
        req.setDepartmentId(1L);
        req.setManagerId(2L);

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(req));
    }

    @Test
    @DisplayName("createEmployee - should accept maximum salary")
    void createEmployee_ShouldAcceptMaximumSalary() {
        EmployeeRequest req = new EmployeeRequest();
        req.setName("Big Boss");
        req.setPosition("CEO");
        req.setSalary(Long.MAX_VALUE);
        req.setDepartmentId(1L);
        req.setManagerId(2L);

        when(employeeMapper.toEntity(req)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.createEmployee(req);

        assertThat(result).isEqualTo(employeeResponse);
        verify(employeeRepository).save(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue().getSalary()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    @DisplayName("createEmployee - should throw exception when name is empty")
    void createEmployee_ShouldThrowException_WhenNameIsEmpty() {
        EmployeeRequest req = new EmployeeRequest();
        req.setName("");
        req.setPosition("Developer");
        req.setSalary(1000L);
        req.setDepartmentId(1L);
        req.setManagerId(2L);

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(req));
    }
}

