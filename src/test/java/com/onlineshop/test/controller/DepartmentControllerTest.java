package com.onlineshop.test.controller;

import com.onlineshop.test.dto.response.DepartmentResponse;
import com.onlineshop.test.exception.DepartmentNotFoundException;
import com.onlineshop.test.service.DepartmentService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration tests
@WebMvcTest(DepartmentController.class)
public class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    @DisplayName("Test getAllDepartments - Validation happy flow")
    void getAllDepartments_ShouldReturnListOfDepartments() throws Exception {
        var department1 = new DepartmentResponse(1L, "Department_01", "loc 01");
        var department2 = new DepartmentResponse(2L, "Department_02", "loc 02");

        when(departmentService.getAllDepartments()).thenReturn(List.of(department1, department2));

        mockMvc.perform(get("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        Mockito.verify(departmentService, Mockito.times(1)).getAllDepartments();
    }

    @Test
    @DisplayName("Test getAllDepartments - Validation empty list")
    void getAllDepartments_ShouldReturnEmptyListWhenNoDepartmentsExist() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of());

        mockMvc.perform(get("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(departmentService, Mockito.times(1)).getAllDepartments();
    }

    @Test
    @DisplayName("Test getDepartmentById - Validation happy flow")
    void getDepartmentById_ShouldReturnDepartment_WhenDepartmentExists() throws Exception {
        var departmentId = 1L;
        var departmentResponse = new DepartmentResponse(departmentId, "Department_01", "loc 01");

        when(departmentService.getDepartmentById(departmentId)).thenReturn(departmentResponse);

        mockMvc.perform(get("/api/departments/{id}", departmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Department_01"));

        Mockito.verify(departmentService, Mockito.times(1)).getDepartmentById(departmentId);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2})
    @DisplayName("Test getDepartmentById - Validation dep not found")
    void getDepartmentById_ShouldReturnNotFound_WhenDepartmentDoesNotExist(@NotNull Long departmentId) throws Exception {
        when(departmentService.getDepartmentById(departmentId)).thenThrow(new DepartmentNotFoundException(departmentId));

        mockMvc.perform(get("/api/departments/{id}", departmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(departmentService, Mockito.times(1)).getDepartmentById(departmentId);
    }
}