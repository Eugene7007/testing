package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    EmployeeService employeeService;

    @Test
    void getAllEmployees() throws Exception {
        Mockito.when(employeeService.getAllEmployees())
                .thenReturn(List.of(new EmployeeResponse(1L, "AkbarXoja", "DevJavA", BigDecimal.TEN, null, null)));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void getAllEmployees_Empty() throws Exception {
        Mockito.when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void getEmployeeById() throws Exception {
        Mockito.when(employeeService.getEmployeeById(1L))
                .thenReturn(new EmployeeResponse(1L, "A", "Dev", BigDecimal.TEN, null, null));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }


    @Test
    void getEmployeeById_Error() throws Exception {
        Mockito.when(employeeService.getEmployeeById(100L))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/employees/100"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void createEmployee() throws Exception {
        var req = new EmployeeRequest("Azamat", "Devops", BigDecimal.TEN, null, null);
        var resp = new EmployeeResponse(1L, "Azamat", "Devops", BigDecimal.TEN, null, null);

        Mockito.when(employeeService.createEmployee(any())).thenReturn(resp);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }


    @Test
    void createEmployee_ValidationError() throws Exception {
        var req = new EmployeeRequest("", "Dev", BigDecimal.TEN, null, null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void updateEmployee() throws Exception {
        var req = new EmployeeRequest("Dilurod", "UzumDev", BigDecimal.ONE, null, null);
        var resp = new EmployeeResponse(1L, "Dilmurod", "UzumDev", BigDecimal.ONE, null, null);

        Mockito.when(employeeService.updateEmployee(eq(1L), any())).thenReturn(resp);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }


    @Test
    void updateEmployee_ValidationError() throws Exception {
        var req = new EmployeeRequest("", "", null, null, null);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk());
    }


    @Test
    void deleteEmployee_Error() throws Exception {
        Mockito.doThrow(new RuntimeException("Not found"))
                .when(employeeService).deleteEmployee(50L);

        mockMvc.perform(delete("/api/employees/50"))
                .andExpect(status().isInternalServerError());
    }
}
