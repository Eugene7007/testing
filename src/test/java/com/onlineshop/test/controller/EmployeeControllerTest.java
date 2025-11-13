package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;


@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("Test getAllEmployees - Validation happy flow")
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        var emp1 = new EmployeeResponse(1L, "John Doe", "DEVELOPER", 70000L, "Engineering", "Alice");
        var emp2 = new EmployeeResponse(2L, "Jane Smith", "MANAGER", 90000L, "HR", null);
        when(employeeService.getAllEmployees()).thenReturn(List.of(emp1, emp2));

        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getAllEmployees - Validation empty list")
    void getAllEmployees_ShouldReturnEmptyListWhenNoEmployeesExist() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees").contentType(MediaType.APPLICATION_JSON)).andExpect(
            status().isOk()).andExpect(jsonPath("$", hasSize(0)));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getEmployeeById - Validation happy flow")
    void getEmployeeById_ShouldReturnEmployee_WhenEmployeeExists() throws Exception {
        Long id = 1L;
        var response = new EmployeeResponse(id, "Ted Mosby", "Architect", 85000L, "Backend", "Mike");

        when(employeeService.getEmployeeById(id)).thenReturn(response);

        mockMvc.perform(get("/api/employees/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Ted Mosby"));

        verify(employeeService, times(1)).getEmployeeById(id);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2})
    @DisplayName("Test getEmployeeById - Validation employee not found")
    void getEmployeeById_ShouldReturnNotFound_WhenEmployeeDoesNotExist(Long id) throws Exception {
        when(employeeService.getEmployeeById(id)).thenThrow(new EmployeeNotFoundException(id));

        mockMvc.perform(get("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(id);
    }

    @Test
    @DisplayName("Test createEmployee - Validation happy flow")
    void createEmployee_ShouldCreateAndReturnEmployee() throws Exception {
        var request = new EmployeeRequest("Jon", "ANALYST", 65000L, 1L, null);
        var response = new EmployeeResponse(3L, "Jon", "ANALYST", 65000L, "Analytics", null);

        when(employeeService.createEmployee(request)).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.position").value("ANALYST"))
            .andExpect(jsonPath("$.name").value("Jon"))
            .andExpect(jsonPath("$.salary").value(65000L));

        verify(employeeService, times(1)).createEmployee(request);
    }

    @Test
    @DisplayName("Test createEmployee - Validation invalid salary (negative)")
    void createEmployee_ShouldReturnBadRequest_WhenSalaryIsNull() throws Exception {
        var invalidRequest = new EmployeeRequest("Invalid User", "TESTER", -10L, 2L, null);

        mockMvc.perform(post("/api/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @DisplayName("Test updateEmployee - Validation happy flow")
    void updateEmployee_ShouldUpdateAndReturnEmployee() throws Exception {
        Long id = 1L;
        var request = new EmployeeRequest("John Updated", "TEAM_LEAD", 95000L, 1L, null);
        var response = new EmployeeResponse(id, "John Updated", "TEAM_LEAD", 95000L, "Engineering", "CEO");

        when(employeeService.updateEmployee(id, request)).thenReturn(response);

        mockMvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.position").value("TEAM_LEAD")).andExpect(jsonPath("$.salary").value(95000)).andExpect(jsonPath("$.managerName").value("CEO"));

        verify(employeeService, times(1)).updateEmployee(id, request);
    }

    @Test
    @DisplayName("Test updateEmployee - Validation employee not found")
    void updateEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception {
        Long id = 999L;
        var request = new EmployeeRequest("Ghost", "PHANTOM", 1L, 999L, 1L);

        when(employeeService.updateEmployee(id, request)).thenThrow(new EmployeeNotFoundException(id));

        mockMvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(id, request);
    }

    @Test
    @DisplayName("Test deleteEmployee - Validation happy flow")
    void deleteEmployee_ShouldDeleteEmployee_WhenExists() throws Exception {
        Long id = 1L;
        doNothing().when(employeeService).deleteEmployee(id);

        mockMvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isOk());

        verify(employeeService, times(1)).deleteEmployee(id);
    }

    @ParameterizedTest
    @ValueSource(longs = {500L, 600L})
    @DisplayName("Test deleteEmployee - Validation employee not found")
    void deleteEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist(Long id) throws Exception {
        doThrow(new EmployeeNotFoundException(id)).when(employeeService).deleteEmployee(id);

        mockMvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployee(id);
    }

    @Test
    @DisplayName("Test createEmployee - Validation blank name triggers validation error")
    void createEmployee_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        var request = new EmployeeRequest("   ", "DEV", 50000L, 1L, null);

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any());
    }
}
