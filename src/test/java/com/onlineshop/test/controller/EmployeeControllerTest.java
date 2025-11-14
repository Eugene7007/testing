package com.onlineshop.test.controller;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.service.EmployeeService;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeResponse employeeResponse;
    private EmployeeRequest employeeRequest;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Department 1");
        department.setLocation("Tashkent");

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setName("Alice Doe");
        manager.setPosition("Developer");
        manager.setSalary(5000L);
        manager.setDepartment(department);

        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setPosition("Developer");
        employee.setSalary(5000L);
        employee.setDepartment(department);
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
    @DisplayName("Test getAllEmployees - should return list of employees")
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(employeeResponse));

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getAllEmployees - should return empty list when no employees exist")
    void getAllEmployees_ShouldReturnEmptyListWhenNoEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getEmployeeById - should return employee when exists")
    void getEmployeeById_ShouldReturnEmployee_WhenEmployeeExists() throws Exception {
        Long employeeId = 1L;
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employeeResponse);

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(employeeId);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2})
    @DisplayName("Test getEmployeeById - should return 404 when employee does not exist")
    void getEmployeeById_ShouldReturnNotFound_WhenEmployeeDoesNotExist(@NotNull Long employeeId) throws Exception {
        when(employeeService.getEmployeeById(employeeId)).thenThrow(new EmployeeNotFoundException(employeeId));

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(employeeId);
    }

    @Test
    @DisplayName("Test createEmployee - should create a new employee")
    void createEmployee_ShouldCreateNewEmployee() throws Exception {
        when(employeeService.createEmployee(Mockito.any(EmployeeRequest.class))).thenReturn(employeeResponse);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"position\":\"Developer\",\"salary\":5000,\"departmentId\":1,\"managerId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));

        Mockito.verify(employeeService, Mockito.times(1)).createEmployee(Mockito.any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Test updateEmployee - should update employee when exists")
    void updateEmployee_ShouldUpdateEmployee() throws Exception {
        Long employeeId = 1L;
        when(employeeService.updateEmployee(Mockito.eq(employeeId), Mockito.any(EmployeeRequest.class))).thenReturn(employeeResponse);

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"position\":\"Developer\",\"salary\":5000,\"departmentId\":1,\"managerId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));

        Mockito.verify(employeeService, Mockito.times(1)).updateEmployee(Mockito.eq(employeeId), Mockito.any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Test deleteEmployee - should delete employee when exists")
    void deleteEmployee_ShouldDeleteEmployee() throws Exception {
        Long employeeId = 1L;

        mockMvc.perform(delete("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(employeeId);
    }
}
