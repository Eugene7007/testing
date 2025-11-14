package com.onlineshop.test.controller;

import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("Test getAllEmployees - Validation happy flow")
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        var e1 = new EmployeeResponse(1L, "Bob", "Specialist", 100L, "IT", "Tom");
        var e2 = new EmployeeResponse(2L, "David", "Specialist", 150L, "IT", "Tom");

        when(employeeService.getAllEmployees()).thenReturn(List.of(e1 ,e2));

        mockMvc.perform(get("api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        Mockito.verify(employeeService).getAllEmployees();
    }

}
