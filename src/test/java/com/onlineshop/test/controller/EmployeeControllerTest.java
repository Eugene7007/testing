package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.service.EmployeeService;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("getAllEmployees then should return list of employees")
    public void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        var employee1 = new EmployeeResponse(1L, "Bob", "Engineer", 1_000L, "IT", "John");
        var employee2 = new EmployeeResponse(2L, "Michael", "Accounter", 2_000L, "Accounting", "John");
        var employee3 = new EmployeeResponse(3L, "Louise", "Director", 3_000L, "Main", "Elon");

        when(employeeService.getAllEmployees()).thenReturn(List.of(employee1, employee2, employee3));

        mockMvc.perform(get("/api/employees")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("getAllEmployees then should return empty list")
    public void getAllEmployees_ShouldReturnEmptyList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("getEmployeeById then should return employee when employee exists")
    public void getEmployeeById_ShouldReturnEmployee_WhenEmployeeExists() throws Exception {
        var employeeId = 1L;
        var employee = new EmployeeResponse(1L, "Bob", "Engineer", 1_000L, "IT", "John");

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);

        mockMvc.perform(get("/api/employees/" + employeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bob"));

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(employeeId);
    }

    @Test
    @DisplayName("getEmployeeById then should return not found when employee does not exist")
    public void getEmployeeById_ShouldReturnNotFound_WhenEmployeeDoesNotExist(@NotNull Long employeeId) throws Exception {
        when(employeeService.getEmployeeById(employeeId))
            .thenThrow(new EmployeeNotFoundException(employeeId));

        mockMvc.perform(get("/api/employees/" + employeeId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(employeeId);
    }

    @Test
    @DisplayName("createEmployee then should return employee when employee is valid")
    public void createEmployee_ShouldReturnEmployee_WhenEmployeeIsValid() throws Exception {
        var request = new EmployeeRequest();
        request.setName("Bob");
        request.setPosition("Engineer");
        request.setSalary(1_000L);
        request.setDepartmentId(1L);
        request.setManagerId(1L);

        var response = new EmployeeResponse(1L, "Bob", "Engineer", 1_000L, "IT", "John");

        when(employeeService.createEmployee(request)).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Bob"))
            .andExpect(jsonPath("$.position").value("Engineer"))
            .andExpect(jsonPath("$.salary").value(1_000L));

        Mockito.verify(employeeService, Mockito.times(1)).createEmployee(request);
    }

    @Test
    @DisplayName("createEmployee then should return bad request when request is invalid")
    public void createEmployee_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        var request = new EmployeeRequest();

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists()) //validation error here
                .andExpect(jsonPath("$.position").exists()) //validation error here
                .andExpect(jsonPath("$.salary").exists()); //validation error here
    }

    @Test
    @DisplayName("createEmployee then should return bad request when name is blank")
    public void createEmployee_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        var request = new EmployeeRequest();
        request.setName("");
        request.setPosition("Engineer");
        request.setSalary(1_000L);
        request.setDepartmentId(1L);
        request.setManagerId(1L);

        mockMvc.perform(post("/api/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name").value("Имя сотрудника не может быть пустым"));

        Mockito.verify(employeeService, never()).createEmployee(request);
    }

    @Test
    @DisplayName("updateEmployee then should return employee when employee is valid")
    public void updateEmployee_ShouldReturnEmployee_WhenEmployeeIsValid(@NotNull Long employeeId) throws Exception {
        var request = new EmployeeRequest();
        request.setName("Bob");
        request.setPosition("Engineer");
        request.setSalary(2_000L);
        request.setDepartmentId(1L);
        request.setManagerId(1L);

        var expectedResponse = new EmployeeResponse(1L, "Bob",
            "Engineer", 1_000L, "IT", "John");

        when(employeeService.updateEmployee(employeeId, request)).thenReturn(expectedResponse);

        mockMvc.perform(put("/api/employees/" + employeeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bob"))
            .andExpect(jsonPath("$.position").value("Engineer"))
            .andExpect(jsonPath("$.salary").value(2_000L));

        Mockito.verify(employeeService, Mockito.times(1)).updateEmployee(employeeId, request);
    }

    @Test
    @DisplayName("updateEmployee then should return not found when employee does not exist")
    public void updateEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist(@NotNull Long employeeId) throws Exception {
        var request = new EmployeeRequest();
        request.setName("Bob");
        request.setPosition("Engineer");
        request.setSalary(2_000L);
        request.setDepartmentId(1L);
        request.setManagerId(1L);

        when(employeeService.updateEmployee(employeeId, request)).thenThrow(new EmployeeNotFoundException(employeeId));

        mockMvc.perform(put("/api/employees/" + employeeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).updateEmployee(employeeId, request);
    }

    @Test
    @DisplayName("updateEmployee then should return bad request when request is invalid")
    public void updateEmployee_ShouldReturnBadRequest_WhenRequestIsInvalid(@NotNull Long employeeId) throws Exception {
        var request = new EmployeeRequest();
        request.setName("Bob");
        request.setPosition("");
        request.setSalary(2_000L);
        request.setDepartmentId(1L);
        request.setManagerId(1L);

        mockMvc.perform(put("/api/employees/" + employeeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.position").value("Должность сотрудника не может быть пустой"));

        Mockito.verify(employeeService, never()).updateEmployee(employeeId, request);
    }

    @Test
    @DisplayName("deleteEmployeeById then should delete employee when employee exists")
    public void deleteEmployeeById_ShouldDeleteEmployee_WhenEmployeeExists(@NotNull Long employeeId) throws Exception {

        doNothing().when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete("/api/employees/" + employeeId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(employeeId);
    }

    @Test
    @DisplayName("deleteEmployeeById then should return not found when employee does not exist")
    public void deleteEmployeeById_ShouldReturnNotFound_WhenEmployeeDoesNotExist(@NotNull Long employeeId) throws Exception {
        doThrow(new EmployeeNotFoundException(employeeId)).when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete("/api/employees/" + employeeId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployee(employeeId);
    }
}