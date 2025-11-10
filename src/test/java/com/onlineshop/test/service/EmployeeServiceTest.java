package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Validator validator;
    private Set<ConstraintViolation<EmployeeRequest>> validate;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void getAllEmployees_Success() {
        Employee emp = new Employee(1L, "Mukhammadjon",
                "Developer", 3000L, new Department(), null);

        Employee emp1 = new Employee(2L, "Samandar",
                "Developer", 2000L, new Department(), emp);

        List<Employee> employees = List.of(emp, emp1);

        EmployeeResponse empResponse = new EmployeeResponse(1L, "Mukhammadjon",
                "Developer", 3000L, "IT", null);

        EmployeeResponse empResponse1 = new EmployeeResponse(2L, "Samandar",
                "Developer", 2000L, "IT", "Mukhammadjon");

        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.toResponse(emp)).thenReturn(empResponse);
        when(employeeMapper.toResponse(emp1)).thenReturn(empResponse1);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Mukhammadjon", result.get(0).name());
        assertEquals("Samandar", result.get(1).name());
    }


    @Test
    void getEmployeeById_Success() {
        long empId = 1L;
        Employee employee = new Employee();

        EmployeeResponse empResponse = new EmployeeResponse(1L, "Mukhammadjon",
                "Developer", 3000L, "IT", null);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(empResponse);

        EmployeeResponse response = employeeService.getEmployeeById(empId);

        assertNotNull(response);
        assertEquals(empResponse, response);
    }

    @Test
    void getEmployeeById_ThrowsException() {
        long empId = 1L;

        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(empId));
    }

    @Test
    void createEmployee_Success() {

        EmployeeRequest empRequest = new EmployeeRequest();

        Employee employee = new Employee(1L, "Mukhammadjon",
                "Developer", 3000L, new Department(), null);

        EmployeeResponse empResponse = new EmployeeResponse(1L, "Mukhammadjon",
                "Developer", 3000L, "IT", null);
        when(employeeMapper.toEntity(empRequest)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(empResponse);

        EmployeeResponse response = employeeService.createEmployee(empRequest);

        assertNotNull(response);
        assertEquals(empResponse, response);
    }

    @Test
    void createEmployee_InvalidName() {
        EmployeeRequest empRequest = new EmployeeRequest();
        empRequest.setName(null);
        empRequest.setPosition("It");
        empRequest.setSalary(2000L);
        empRequest.setDepartmentId(1L);

        validate = validator.validate(empRequest);
        assertFalse(validate.isEmpty());
    }

    @Test
    void createEmployee_InvalidSalary() {
        EmployeeRequest empRequest = new EmployeeRequest();
        empRequest.setName("null");
        empRequest.setPosition("It");
        empRequest.setSalary(-12L);
        empRequest.setDepartmentId(1L);

        validate = validator.validate(empRequest);
        assertFalse(validate.isEmpty());
    }

    @Test
    void createEmployee_InvalidPosition() {
        EmployeeRequest empRequest = new EmployeeRequest();
        empRequest.setName("null");
        empRequest.setPosition(null);
        empRequest.setSalary(12L);
        empRequest.setDepartmentId(1L);

        validate = validator.validate(empRequest);
        assertFalse(validate.isEmpty());
    }

    @Test
    void updateEmployee_Success() {
        long empId = 1L;

        EmployeeRequest empRequest = new EmployeeRequest();
        empRequest.setName("Test");
        empRequest.setPosition("Developer");
        empRequest.setSalary(4000L);
        empRequest.setDepartmentId(1L);

        Employee employee = new Employee(1L, "Mukhammadjon",
                "Developer", 3000L, new Department(), null);

        EmployeeResponse empResponse = new EmployeeResponse(1L, "Test",
                "Developer", 4000L, "IT", null);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(empResponse);

        EmployeeResponse response = employeeService.updateEmployee(empId, empRequest);

        assertNotNull(response);
        assertEquals(empResponse, response);
    }

    @Test
    void updateEmployee_EmployeeNotFound_ThrowsException() {
        long empId = 1L;
        EmployeeRequest empRequest = new EmployeeRequest();
        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(empId, empRequest));
    }

    @Test
    void deleteEmployee_Success() {
        long empId = 1L;
        Employee employee = new Employee(1L, "Mukhammadjon",
                "Developer", 3000L, new Department(), null);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(empId);
        verify(employeeRepository, times(1)).findById(empId);
        verify(employeeRepository, times(1)).deleteById(empId);
    }

    @Test
    void deleteEmployee_EmployeeNotFound_ThrowsException() {
        long empId = 1L;

        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(empId));
    }
}
