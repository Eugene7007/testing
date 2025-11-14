package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        var employee1 = new EmployeeResponse(1L,"yuriy","software engineer",1500L,"It",null);
        var employee2 = new EmployeeResponse(2L,"anna","data analyst",1500L,"analytics",null);

        when(employeeService.getAllEmployees()).thenReturn(List.of(employee1,employee2));

        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee_WhenExists() throws Exception {
        var employeeId = 1L;
        var response = new EmployeeResponse(employeeId, "Yuriy", "Engineer", 1500L, "IT", null);

        when(employeeService.getEmployeeById(employeeId)).thenReturn(response);

        mockMvc.perform(get("/api/employees/{id}", employeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Yuriy"));

        Mockito.verify(employeeService).getEmployeeById(employeeId);
    }

    @Test
    void getEmployeeById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        var employeeId = 100L;

        when(employeeService.getEmployeeById(employeeId))
            .thenThrow(new EmployeeNotFoundException(employeeId));

        mockMvc.perform(get("/api/employees/{id}", employeeId))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService).getEmployeeById(employeeId);
    }

    @Test
    void createEmployee_ShouldCreateNewEmployee() throws Exception {
        var request = new EmployeeRequest();
        request.setName("Yuriy");
        request.setPosition("Engineer");
        request.setSalary(5000L);
        request.setDepartmentId(1L);

        var response = new EmployeeResponse(1L, "Yuriy", "Engineer", 5000L, "It", null);

        when(employeeService.createEmployee(request)).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));

        Mockito.verify(employeeService).createEmployee(request);
    }

    @Test
    void createEmployee_ShouldReturnBadRequest_WhenInvalid() throws Exception {
        var request = new EmployeeRequest();
        request.setName(null);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmployee_ShouldUpdateEmployee_WhenExists() throws Exception {
        var employeeId = 1L;
        var request = new EmployeeRequest();
        request.setName("Anna");
        request.setPosition("Data Analyst");
        request.setSalary(2000L);
        request.setDepartmentId(1L);
        var response = new EmployeeResponse(employeeId, "Anna", "Data Analyst", 2000L, "Analytics", null);

        when(employeeService.updateEmployee(employeeId, request)).thenReturn(response);

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Anna"));

        Mockito.verify(employeeService).updateEmployee(employeeId, request);
    }

    @Test
    void updateEmployee_ShouldReturnNotFound_WhenNotExists() throws Exception {
        var employeeId = 5L;
        var request = new EmployeeRequest();
        request.setName("Test");
        request.setPosition("Dev");
        request.setSalary(1500L);
        request.setDepartmentId(1L);

        when(employeeService.updateEmployee(employeeId, request))
            .thenThrow(new EmployeeNotFoundException(employeeId));

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService).updateEmployee(employeeId, request);
    }

    @Test
    void updateEmployee_ShouldReturnBadRequest_WhenInvalid() throws Exception {
        var request = new EmployeeRequest();
        request.setName(null);

        mockMvc.perform(put("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployee_ShouldDelete_WhenExists() throws Exception {
        var employeeId = 1L;

        doNothing().when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete("/api/employees/{id}", employeeId))
            .andExpect(status().isOk());

        Mockito.verify(employeeService).deleteEmployee(employeeId);
    }

    @Test
    void deleteEmployee_ShouldReturnNotFound_WhenNotExists() throws Exception {
        var employeeId = 123L;

        doThrow(new EmployeeNotFoundException(employeeId))
            .when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete("/api/employees/{id}", employeeId))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService).deleteEmployee(employeeId);
    }
}


