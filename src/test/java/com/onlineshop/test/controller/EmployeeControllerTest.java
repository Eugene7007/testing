package com.onlineshop.test.controller;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.DepartmentResponse;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.DepartmentNotFoundException;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import com.onlineshop.test.service.DepartmentService;
import com.onlineshop.test.service.EmployeeService;
import jakarta.validation.constraints.NotNull;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;



@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;
    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("Test getAllEmployees - Validation happy flow")
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception{
        EmployeeResponse employeeResponse1 = new EmployeeResponse(1L, "Dante", "Unemployed",100L, "Devil May Cry", null);
        EmployeeResponse employeeResponse2 = new EmployeeResponse(2L, "Vergil", "Unemployed parent", 200L, null, null);

        when(employeeService.getAllEmployees()).thenReturn(List.of(employeeResponse1, employeeResponse2));

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getAllEmployees - Validation empty list")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExist() throws Exception{

        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @ParameterizedTest
    @ValueSource(longs = {1,2})
    @DisplayName("Test getEmployeeById - Validation employee not found")
    void getEmployeeById_ShouldReturnNotFound_WhenEmployeeDoesNotExist(@NotNull Long employeeId) throws Exception{

        when(employeeService.getEmployeeById(employeeId)).thenThrow(new EmployeeNotFoundException(employeeId));

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(employeeId);
    }

    @ParameterizedTest
    @ValueSource(longs = {1,2})
    @DisplayName("Test getEmployeeId - Validation employee found")
    void getEmployeeById_ShouldReturnEmployee_WhenEmployeeExists() throws Exception{
        Long employeeId = 1L;
        EmployeeResponse employeeResponse1 = new EmployeeResponse(1L, "Dante", "Unemployed",100L, "Devil May Cry", null);

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employeeResponse1);

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(employeeId);
    }

    //возвращает код 200, вместо 201, не понимаю в чём проблема
    @Test
    @DisplayName("Test createEmployee")
    void createEmployee_ShouldCreateEmployee() throws Exception{
        EmployeeRequest employeeRequest = new EmployeeRequest();
        employeeRequest.setName("V");
        employeeRequest.setPosition("Unemployed");
        employeeRequest.setSalary(100L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(null);
        EmployeeResponse employeeResponse = new EmployeeResponse(6L, "V", "Unemployed",100L, "Devil May Cry", null);
        when(employeeService.createEmployee(employeeRequest)).thenReturn(employeeResponse);
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"V\", \"position\":\"Unemployed\", \"salary\":100, \"departmentId\":1, \"managerId\":null}"))
                .andExpect(status().isCreated())
                .andReturn();

    }

    @Test
    @DisplayName("Test updateEmployee - Validation employee found")
    void updateEmployee_ShouldUpdateEmployee_WhenEmployeeExist() throws Exception{
        Long employeeId = 1L;
        EmployeeRequest employeeRequest = new EmployeeRequest();
        EmployeeResponse employeeResponse = new EmployeeResponse(1L, "Dante", "Unemployed",200L, "Devil May Cry", null);
        when(employeeService.updateEmployee(employeeId, employeeRequest)).thenReturn(employeeResponse);
        mockMvc.perform(put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Dante\", \"position\":\"Unemployed\", \"salary\":200, \"departmentId\":1, \"managerId\":null}"))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Test updateEmployee - Validation employee not found")
    void updateEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception{
        Long employeeId = 5L;
        EmployeeRequest employeeRequest = new EmployeeRequest();
        employeeRequest.setName("Nero");
        employeeRequest.setPosition("Unemployed");
        employeeRequest.setSalary(90L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(null);

        when(employeeService.updateEmployee(employeeId, employeeRequest)).thenThrow(new EmployeeNotFoundException(employeeId));
        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Nero\", \"position\":\"Unemployed\", \"salary\":90, \"departmentId\":1, \"managerId\":null}"))
                .andExpect(status().isNotFound());

    }


    @Test
    @DisplayName("Test deleteEmployee - Validation employee found")
    void deleteEmployee_ShouldDeleteEmployee_WhenEmployeeExists() throws Exception{
        Long employeeId = 1L;

        mockMvc.perform(delete("/api/employees/{id}", employeeId))
                        .andExpect(status().isOk());

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(employeeId);
    }

    @Test
    @DisplayName("Test deleteEmployee - Validation employee not found")
    void deleteEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception{
        Long employeeId = 1L;
        Mockito.doThrow(new EmployeeNotFoundException(employeeId)).when(employeeService).deleteEmployee(employeeId);
        mockMvc.perform(delete("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(employeeId);
    }
}
