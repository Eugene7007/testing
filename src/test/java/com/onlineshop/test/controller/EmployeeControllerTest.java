package com.onlineshop.test.controller;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("ALL")
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee savedEmployee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        Employee employee = new Employee();
        employee.setName("John");
        employee.setSalary(5000L);
        savedEmployee = employeeRepository.save(employee);
    }

    //test1
    @Test
    void testGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    //test2
    @Test
    void testGetEmployeeById() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", savedEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John")));
    }

    //test3
    @Test
    void testGetEmployeeByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    //test4
    @Test
    void testGetEmployeeByIdException() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", 999L))
                .andExpect(status().isInternalServerError());
    }

    //test5
    @Test
    void testCreateEmployee() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Davy");
        request.setSalary(10000L);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Davy")));
    }

    //test6
    @Test
    void testUpdateEmployee() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Davy");
        request.setSalary(30000L);

        mockMvc.perform(post("/api/employees/{id}", savedEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOK())
                .andExpect(jsonPath("$.name", is("Davy")))
                .andExpect(jsonPath("$.salary", is(30000L)));
    }

    //test7
    @Test
    void testUpdateEmployee_NotFound() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Test");
        request.setSalary(1000L);

        mockMvc.perform(put("/api/employees/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    //test8
    @Test
    void testDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", savedEmployee.getId()))
                .andExpect(status().isNoContent());
    }

    //test9
    @Test
    void testDeleteEmployee_NotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    //test10
    @Test
    void testCreateEmployee_InvalidRequest() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        //name is null
        request.setSalary(-10000L);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    //test11
    @Test
    void testAllEmployees_Empty() throws Exception {
        employeeRepository.deleteAll();
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    //test12
    @Test
    void testCreateMultipleEmployees() throws Exception {
        EmployeeRequest request1 = new EmployeeRequest();
        request1.setName("Emp1");
        request1.setSalary(1000L);

        EmployeeRequest request2 = new EmployeeRequest();
        request2.setName("Emp2");
        request2.setSalary(2000L);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}