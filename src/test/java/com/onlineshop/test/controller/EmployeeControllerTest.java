package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.service.EmployeeService;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test getAllEmployees - Success")
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        var employee1 = new EmployeeResponse(1L, "Mukhammadjon", "developer", 100000L, "IT", null);
        var employee2 = new EmployeeResponse(2L, "Sardor", "hr", 10000L, "HR", null);

        when(employeeService.getAllEmployees()).thenReturn(List.of(employee1, employee2));

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Mukhammadjon")))
                .andExpect(jsonPath("$[1].name", is("Sardor")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getAllEmployees - Empty List Success")
    void getAllEmployees_ShouldReturnEmptyListOfEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getEmployeeById - Success")
    void getEmployeeById_ShouldReturnEmployee_WhenEmployeeExists() throws Exception {
        var employeeId = 1L;
        var employee1 = new EmployeeResponse(1L, "Mukhammadjon", "developer", 100000L, "IT", null);

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee1);

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Mukhammadjon")));

        verify(employeeService, times(1)).getEmployeeById(employeeId);
    }

    @ParameterizedTest
    @ValueSource(longs = {900, 2000, 3000})
    @DisplayName("Test getEmployeeById - Not Found")
    void getEmployeeById_ShouldReturnNotFound_WhenEmployeeDoesntExists(@NotNull Long employeeId) throws Exception {
        when(employeeService.getEmployeeById(employeeId)).thenThrow(new EmployeeNotFoundException(employeeId));

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(employeeId);
    }

    @Test
    @DisplayName("Test createEmployee - Success")
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        var request = new EmployeeRequest();
        request.setName("Mukhammadjon");
        request.setPosition("developer");
        request.setSalary(100000L);
        request.setDepartmentId(1L);
        request.setManagerId(null);
        var response = new EmployeeResponse(1L, "Mukhammadjon", "developer", 100000L, "IT", null);

        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Mukhammadjon")));

        verify(employeeService, times(1)).createEmployee(request);
    }

    @Test
    @DisplayName("Test createEmployee - Invalid Request")
    void createEmployee_ShouldReturnBadRequest_InvalidData() throws Exception {
        var invalidRequest = new EmployeeRequest();
        invalidRequest.setName("");
        invalidRequest.setPosition("");
        invalidRequest.setSalary(-10L);
        invalidRequest.setDepartmentId(100L);
        invalidRequest.setManagerId(null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(0)).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Test updateEmployee - Success")
    void updateEmployee_ShouldReturnUpdatedEmployee() throws Exception {
        var employeeId = 1L;
        var request = new EmployeeRequest();
        request.setName("Mukhammadjon");
        request.setPosition("senior developer");
        request.setSalary(150000L);
        request.setDepartmentId(1L);
        request.setManagerId(null);
        var response = new EmployeeResponse(employeeId, "Mukhammadjon", "senior developer", 150000L, "IT", null);

        when(employeeService.updateEmployee(eq(employeeId), any(EmployeeRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.position", is("senior developer")))
                .andExpect(jsonPath("$.salary", is(150000)));

        verify(employeeService, times(1)).updateEmployee(eq(employeeId), any(EmployeeRequest.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {900, 2000, 3000})
    @DisplayName("Test updateEmployee - Not found")
    void updateEmployee_ShouldReturnNotFound_WhenEmployeeDoesntExists(@NotNull Long employeeId) throws Exception {
        var request = new EmployeeRequest();
        request.setName("Mukhammadjon");
        request.setPosition("developer");
        request.setSalary(100000L);
        request.setDepartmentId(1L);
        request.setManagerId(null);

        when(employeeService.updateEmployee(eq(employeeId), any(EmployeeRequest.class)))
                .thenThrow(new EmployeeNotFoundException(employeeId));

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(eq(employeeId), any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Test updateEmployee - Invalid Request")
    void updateEmployee_ShouldReturnBadRequest_InvalidData() throws Exception {
        var employeeId = 1L;
        var invalidRequest = new EmployeeRequest();
        invalidRequest.setName("");
        invalidRequest.setPosition("");
        invalidRequest.setSalary(-10L);
        invalidRequest.setDepartmentId(100L);
        invalidRequest.setManagerId(null);

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(0)).updateEmployee(eq(employeeId), any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Test deleteEmployee - Success")
    void deleteEmployee_ShouldReturnNoContent() throws Exception {
        var employeeId = 1L;

        doNothing().when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).deleteEmployee(employeeId);
    }

    @ParameterizedTest
    @ValueSource(longs = {999, 1000, 5000})
    @DisplayName("Test deleteEmployee - Not Found")
    void deleteEmployee_ShouldReturnNotFound_WhenEmployeeDoesntExists(@NotNull Long employeeId) throws Exception {
        doThrow(new EmployeeNotFoundException(employeeId))
                .when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployee(employeeId);
    }

}
