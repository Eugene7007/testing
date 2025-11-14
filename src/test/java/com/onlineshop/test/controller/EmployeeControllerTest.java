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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeResponse emp(Long id, String name) {
        return new EmployeeResponse(id, name, "Developer", 100000L, "IT", "Bob Manager");
    }

    private EmployeeRequest validRequest() {
        var r = new EmployeeRequest();
        r.setName("Alice");
        r.setPosition("Developer");
        r.setSalary(120000L);
        r.setDepartmentId(1L);
        r.setManagerId(10L);
        return r;
    }

    @Test
    @DisplayName("GET /api/employees — список сотрудников")
    void getAllEmployees_ShouldReturnList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(emp(1L, "Alice"), emp(2L, "Carol")));

        mockMvc.perform(get("/api/employees").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("Alice"))
            .andExpect(jsonPath("$[1].name").value("Carol"));

        Mockito.verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("GET /api/employees — пустой список")
    void getAllEmployees_ShouldReturnEmpty() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("GET /api/employees/{id} — найден")
    void getEmployeeById_ShouldReturnEmployee() throws Exception {
        var id = 1L;
        when(employeeService.getEmployeeById(id)).thenReturn(emp(id, "Alice"));

        mockMvc.perform(get("/api/employees/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("Alice"))
            .andExpect(jsonPath("$.position").value("Developer"));

        Mockito.verify(employeeService, times(1)).getEmployeeById(id);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L})
    @DisplayName("GET /api/employees/{id}")
    void getEmployeeById_ShouldReturnNotFound(@NotNull Long id) throws Exception {
        when(employeeService.getEmployeeById(id)).thenThrow(new EmployeeNotFoundException(id));

        mockMvc.perform(get("/api/employees/{id}", id))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService, times(1)).getEmployeeById(id);
    }

    @Test
    @DisplayName("POST /api/employees — создаёт при валидном запросе")
    void createEmployee_ShouldReturnCreated() throws Exception {
        var req = validRequest();
        var resp = new EmployeeResponse(10L, req.getName(), req.getPosition(), req.getSalary(), "IT", "Bob Manager");
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.name").value("Alice"));

        Mockito.verify(employeeService, times(1)).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("POST /api/employees")
    void createEmployee_ShouldReturnBadRequest_WhenInvalid() throws Exception {
        var req = new EmployeeRequest();
        req.setName("");
        req.setPosition("");
        req.setSalary(0L);
        req.setDepartmentId(null);
        req.setManagerId(null);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());

        Mockito.verify(employeeService, times(0)).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} — обновляет при валидном запросе")
    void updateEmployee_ShouldReturnUpdated() throws Exception {
        var id = 5L;
        var req = validRequest();
        req.setName("Updated Name");

        when(employeeService.updateEmployee(eq(id), any(EmployeeRequest.class)))
            .thenReturn(new EmployeeResponse(id, "Updated Name", req.getPosition(), req.getSalary(), "IT", "Bob Manager"));

        mockMvc.perform(put("/api/employees/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("Updated Name"));

        Mockito.verify(employeeService, times(1)).updateEmployee(eq(id), any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("PUT /api/employees/{id}")
    void updateEmployee_ShouldReturnNotFound() throws Exception {
        var id = 999L;
        var req = validRequest();

        when(employeeService.updateEmployee(eq(id), any(EmployeeRequest.class)))
            .thenThrow(new EmployeeNotFoundException(id));

        mockMvc.perform(put("/api/employees/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService, times(1)).updateEmployee(eq(id), any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} — удаляет существующего")
    void deleteEmployee_ShouldReturnOk() throws Exception {
        var id = 7L;

        mockMvc.perform(delete("/api/employees/{id}", id))
            .andExpect(status().isOk());

        Mockito.verify(employeeService, times(1)).deleteEmployee(id);
    }

    @Test
    @DisplayName("DELETE /api/employees/{id}")
    void deleteEmployee_ShouldReturnNotFound() throws Exception {
        var id = 777L;
        Mockito.doThrow(new EmployeeNotFoundException(id)).when(employeeService).deleteEmployee(id);

        mockMvc.perform(delete("/api/employees/{id}", id))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService, times(1)).deleteEmployee(id);
    }
}