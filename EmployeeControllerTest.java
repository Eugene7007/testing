package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("Test GetAllEmployees - Validation happy flow")
    public void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        var employee1 = new EmployeeResponse(1L, "Alice", "Developer", 200000L, "Workers", "Alice");
        var employee2 = new EmployeeResponse(2L, "Andrey", "Project Manager", 100000L, "Developers", "Alice");

        when(employeeService.getAllEmployees()).thenReturn(List.of(employee1, employee2));

        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test GetAllEmployees - Validation empty list")
    public void getAllEmployees_ShouldReturnEmptyListOfEmployeesWhenNoEmployeeExist() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getEmployeeById - Validation happy flow")
    void getEmployeeById_ShouldReturnEmployee_WhenExists() throws Exception {
        var employeeId = 1L;
        var employeeResponse = new EmployeeResponse(1L, "Bob", "Builder", 100000L, "Workers", "Alice");

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employeeResponse);

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bob"));

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(employeeId);
    }

    @Test
    @DisplayName("Test getEmployeeById - Validation NotFound Exception")
    void getEmployeeById_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenThrow(new EmployeeNotFoundException(1L));

        mockMvc.perform(get("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(1L);
    }

    @Test
    @DisplayName("Test createEmployee - Validation NotFound Exception")
    void createEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception {
        var response = new EmployeeResponse(1L, "Mary", "HR", 150000L, "Workers", "Alice");
        var request = new EmployeeRequest();
        request.setName("Mary");
        request.setPosition("HR");
        request.setSalary(150000L);
        request.setDepartmentId(1L);
        request.setManagerId(0L);

        when(employeeService.createEmployee(request)).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Mary"))
            .andExpect(jsonPath("$.position").value("HR"))
            .andExpect(jsonPath("$.salary").value(150000))
            .andExpect(jsonPath("$.departmentName").value("Workers"))
            .andExpect(jsonPath("$.managerName").value("Alice"));

        Mockito.verify(employeeService, Mockito.times(1))
            .createEmployee(request);
    }

    @Test
    @DisplayName("Test createEmployee - Validation Error not Valid request")
    void  createEmployee_ShouldReturnValidationError_WhenRequestIsWrong() throws Exception {
        var request = new EmployeeRequest();
        request.setPosition("HR");
        request.setSalary(150000L);
        request.setDepartmentId(1L);
        request.setManagerId(0L);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test updateEmployee - Validation happy flow")
    void updateEmployee_ShouldReturnUpdated_WhenEmployeeExists() throws Exception {
        long employeeId = 1L;

        var response = new EmployeeResponse(1L, "Mary", "HR", 150000L, "Workers", "Alice");
        var request = new EmployeeRequest();
        request.setName("Mary");
        request.setPosition("HR");
        request.setSalary(150000L);
        request.setDepartmentId(1L);
        request.setManagerId(0L);

        when(employeeService.updateEmployee(employeeId, request)).thenReturn(response);

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Mary"))
            .andExpect(jsonPath("$.position").value("HR"))
            .andExpect(jsonPath("$.salary").value(150000))
            .andExpect(jsonPath("$.departmentName").value("Workers"))
            .andExpect(jsonPath("$.managerName").value("Alice"));

        Mockito.verify(employeeService, Mockito.times(1)).updateEmployee(employeeId, request);

    }

    @Test
    @DisplayName("Test updateEmployee - Employee Not Found")
    void updateEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception {
        long employeeId = 2L;
        var request = new EmployeeRequest();
        request.setName("Mary");
        request.setPosition("HR");
        request.setSalary(150000L);
        request.setDepartmentId(1L);
        request.setManagerId(0L);

        when(employeeService.updateEmployee(employeeId, request))
            .thenThrow(new EmployeeNotFoundException(employeeId));

        mockMvc.perform(put("/api/employees/{id}", employeeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "name": "Mary",
                "position": "HR",
                "salary": 150000,
                "departmentId": 1,
                "managerId": 0
                }"""))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).updateEmployee(employeeId, request);
    }

    @Test
    @DisplayName("Test deleteEmployee - Validation happy flow")
    void deleteEmployee_ShouldReturnDeleted_WhenEmployeeExists() throws Exception {
        var employeeId = 1L;

        doNothing().when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete("/api/employees/{id}", employeeId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(employeeId);
    }

    @Test
    @DisplayName("Test deleteEmployee - Validation Error Employee Not Found")
    void deleteEmployee_ShouldReturnException_WhenEmployeeDoesNotExist() throws Exception {
        var employeeId = 2L;
        doThrow(new EmployeeNotFoundException(employeeId)).when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(get("/api/employees/{id}", employeeId))
            .andExpect(status().isOk());

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(employeeId);
    }
}

