package com.onlineshop.test.controller;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.service.EmployeeService;
import jakarta.validation.constraints.NotNull;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("Test getAllEmployees - Validation happy flow")
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        var e1 = new EmployeeResponse(1L, "Sarvar", "Developer", 100L, "IT", "Abror");
        var e2 = new EmployeeResponse(2L, "Akmal", "Developer", 150L, "IT", "Abror");

        when(employeeService.getAllEmployees()).thenReturn(List.of(e1, e2));

        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getAllEmployees - Validation empty list")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExists() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getEmployeeById - happy flow")
    void getEmployeeById_ShouldReturnEmployee() throws Exception {
        var e = new EmployeeResponse(1L, "Sarvar", "Developer", 100L, "IT", "Abror");
        when(employeeService.getEmployeeById(1L)).thenReturn(e);

        mockMvc.perform(get("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sarvar"));

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(1L);
    }

    @ParameterizedTest
    @ValueSource(longs = { 1, 2 })
    @DisplayName("Test getEmployeeById - not found")
    void getEmployeeById_ShouldReturnNotFound(@NotNull Long departmentId) throws Exception {
        when(employeeService.getEmployeeById(departmentId)).thenThrow(new EmployeeNotFoundException(departmentId));

        mockMvc.perform(get("/api/employees/{id}", departmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(departmentId);
    }

    @Test
    @DisplayName("Test createEmployee - happy flow")
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        var response = new EmployeeResponse(1L, "Abror", "Developer", 120L, "IT", null);
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Abror\",\"position\":\"Developer\",\"salary\":120,\"departmentId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Abror"))
                .andExpect(jsonPath("$.position").value("Developer"));

        Mockito.verify(employeeService, Mockito.times(1)).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Test createEmployee - invalid data")
    void createEmployee_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new RuntimeException("Invalid data"));

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"position\":\"\",\"salary\":0,\"departmentId\":1}"))
                .andExpect(status().isInternalServerError());

        Mockito.verify(employeeService, Mockito.times(1)).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Test updateEmployee - happy flow")
    void updateEmployee_ShouldReturnUpdatedEmployee() throws Exception {
        var response = new EmployeeResponse(1L, "Abror", "Developer", 150L, "IT", null);
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Abror\",\"position\":\"Developer\",\"salary\":150,\"departmentId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Abror"))
                .andExpect(jsonPath("$.position").value("Developer"));

        Mockito.verify(employeeService, Mockito.times(1)).updateEmployee(eq(1L), any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Test updateEmployee - not found")
    void updateEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class)))
                .thenThrow(new EmployeeNotFoundException(1L));

        mockMvc.perform(put("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Abror\",\"position\":\"Developer\",\"salary\":150,\"departmentId\":1}"))
                .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).updateEmployee(eq(1L), any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Test deleteEmployee - happy flow")
    void deleteEmployee_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", 1L))
                .andExpect(status().isOk());

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(1L);
    }

    @Test
    @DisplayName("Test deleteEmployee - not found")
    void deleteEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception {
        Mockito.doThrow(new EmployeeNotFoundException(1L))
                .when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/{id}", 1L))
                .andExpect(status().isInternalServerError());

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(1L);
    }
}
