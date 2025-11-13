package com.onlineshop.test.controller;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("Test getAllEmployees - Validation happy flow")
    void getAllEmployees_ReturnsList() throws Exception {
        List<EmployeeResponse> employees = List.of(
            new EmployeeResponse(1L, "Sanjar", "QA", 1000L, null, null),
            new EmployeeResponse(2L, "Sanjar", "Dev", 2000L, null, null)
        );

        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("Sanjar"))
            .andExpect(jsonPath("$[1].position").value("Dev"));
    }

    @Test
    @DisplayName("Test getEmployeeById - Validation happy flow")
    void getEmployeeById_ReturnsEmployee() throws Exception {
        EmployeeResponse response = new EmployeeResponse(1L, "Sanjar", "QA", 1000L, null, null);

        when(employeeService.getEmployeeById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/employees/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Sanjar"))
            .andExpect(jsonPath("$.position").value("QA"));
    }

    @Test
    @DisplayName("Test getEmployeeById - Validation 404 status code of response")
    void getEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(99L))
            .thenThrow(new EntityNotFoundException("Employee not found"));

        mockMvc.perform(get("/api/employees/99"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Test createEmployee - Validation happy flow")
    void createEmployee_CreatesEmployee() throws Exception {
        EmployeeResponse response = new EmployeeResponse(1L, "Sanjar", "QA", 1500L, "IT", "Eugene");

        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "name": "John",
                                "position": "QA",
                                "salary": 1500,
                                "departmentId": 1
                            }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Sanjar"));
    }

    @Test
    @DisplayName("Test createEmployee - Validation 400 status code of response")
    void createEmployee_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "name": "",
                                "position": ""
                            }
                        """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test updateEmployee - Validation happy flow")
    void updateEmployee_UpdatesSuccessfully() throws Exception {
        EmployeeResponse response = new EmployeeResponse(1L, "Sanjar v2", "Dev v2", 1000L, null, null);

        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "name": "Sanjar v2",
                                "position": "Dev v2",
                                "salary": 1000,
                                "departmentId": 1
                            }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Sanjar v2"))
            .andExpect(jsonPath("$.position").value("Dev v2"));
    }

    @Test
    @DisplayName("Test updateEmployee - Validation 400 status code of response")
    void updateEmployee_NotFound() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any()))
            .thenThrow(new EntityNotFoundException("Not found"));

        mockMvc.perform(put("/api/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {"name": "Test"}
                        """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test deleteEmployee - Validation happy flow")
    void deleteEmployee_DeletesSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
            .andExpect(status().isOk());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @DisplayName("Test getAllEmployees - Validate call method one time")
    void verifyGetAllEmployeesCalledOnce() throws Exception {
        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Test getAllEmployees - Check JSON values")
    void checkResponseJsonStructure() throws Exception {
        List<EmployeeResponse> employees = List.of(
            new EmployeeResponse(1L, "Sanjar", "QA", 1000L, null, null)
        );
        when(employeeService.getAllEmployees()).thenReturn(employees);

        String response = mockMvc.perform(get("/api/employees"))
            .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("Sanjar");
        assertThat(response).contains("QA");
    }
}
