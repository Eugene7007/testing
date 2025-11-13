package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("Test get all departments without validation only click")
    void getAllEmployeesTest() throws Exception {
        var employeeResponse0 = new EmployeeResponse(1l, "John", "worker", 500l, "Google", "Mura");
        var employeeResponse1 = new EmployeeResponse(2l, "Tom", "worker", 500l, "Google", "Mura");
        var employeeResponse2 = new EmployeeResponse(3l, "Anna", "worker", 500l, "Google", "Mura");
        var employeeResponse3 = new EmployeeResponse(4l, "Michael", "worker", 500l, "Google", "Mura");

        when(employeeService.getAllEmployees()).thenReturn(List.of(employeeResponse0, employeeResponse1, employeeResponse2, employeeResponse3));

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        Mockito.verify(employeeService, times(1)).getAllEmployees();
    }



    @Test
    @DisplayName("Test for get employee with id which we write here /api/employees/{id}")
    void getEmployeeByIdTest() throws Exception {
        var employeeResponse0 = new EmployeeResponse(1l, "John", "worker", 500l, "Google", "Mura");
        var employeeResponse1 = new EmployeeResponse(2l, "Tom", "worker", 500l, "Google", "Mura");
        var employeeResponse2 = new EmployeeResponse(3l, "Anna", "worker", 500l, "Google", "Mura");
        var employeeResponse3 = new EmployeeResponse(4l, "Michael", "worker", 500l, "Google", "Mura");

        when(employeeService.getEmployeeById(1l)).thenReturn(employeeResponse0);
        when(employeeService.getEmployeeById(2l)).thenReturn(employeeResponse1);
        when(employeeService.getEmployeeById(3l)).thenReturn(employeeResponse2);
        when(employeeService.getEmployeeById(4l)).thenReturn(employeeResponse3);

        mockMvc.perform(get("/api/employees/{id}",1l)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("worker"));

        mockMvc.perform(get("/api/employees/{id}", 2l)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("worker"));


        mockMvc.perform(get("/api/employees/{id}", 3l)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("worker"));


        mockMvc.perform(get("/api/employees/{id}", 4l)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("worker"));


        Mockito.verify(employeeService, times(1)).getEmployeeById(1l);
        Mockito.verify(employeeService, times(1)).getEmployeeById(2l);
        Mockito.verify(employeeService, times(1)).getEmployeeById(3l);
        Mockito.verify(employeeService, times(1)).getEmployeeById(4l);

    }


    @Test
    @DisplayName("create user test")
    void createEmployeeTest() throws Exception {

        var employeeResponseFirst = new EmployeeResponse(1l, "John", "worker", 700l, "Google", "Mura");
        var employeeRequestFirst= new EmployeeRequest( "John", "worker",700l, 500l,  200l);


        var employeeResponseSecond = new EmployeeResponse(2l, "Tom", "worker", 700l, "Yandex", "Mura");
        var employeeRequestSecond= new EmployeeRequest( "Tom", "worker",700l, 500l,  200l);


        when(employeeService.createEmployee(employeeRequestFirst)).thenReturn(employeeResponseFirst);
        when(employeeService.createEmployee(employeeRequestSecond)).thenReturn(employeeResponseSecond);
//для 1
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestFirst))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("worker"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.salary").value(700l));

//для 2
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestSecond))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("worker"))
                .andExpect(jsonPath("$.name").value("Tom"))
                .andExpect(jsonPath("$.salary").value(700l))
                .andExpect(jsonPath("$.departmentName").value("Yandex"));

        Mockito.verify(employeeService, times(1)).createEmployee(employeeRequestFirst);
        Mockito.verify(employeeService, times(1)).createEmployee(employeeRequestSecond);

    }



    @Test
    @DisplayName("Update user test")
    void updateEmployeeTest() throws Exception {
        var employeeResponse1 = new EmployeeResponse(1l, "John", "worker", 700l, "Google", "Mura");
        var employeeRequest1= new EmployeeRequest( "John", "worker",700l, 500l,  200l);


        when(employeeService.updateEmployee(1l, employeeRequest1)).thenReturn(employeeResponse1);

        mockMvc.perform(put("/api/employees/{id}",1l)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest1))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("worker"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.salary").value(700l));

        Mockito.verify(employeeService, times(1)).updateEmployee(1l, employeeRequest1);

    }


    @Test
    void deleteEmployeeTest() throws Exception {

        doNothing().when(employeeService).deleteEmployee(1l);

        mockMvc.perform(delete("/api/employees/{id}", 1l))
                .andExpect(status().isOk());

        Mockito.verify(employeeService, times(1)).deleteEmployee(1l);
    }


    @Test
    @DisplayName("This test should return empty list because we don't have any employee in our system")
    void getAllEmployeeTest_ShouldReturnEmptyListWhenEmployeeDoesNotExist() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(employeeService, times(1)).getAllEmployees();
    }


    @Test
    @DisplayName("In this test should return Not Found exception when we wanna try get user which have id =1")
    void getAllEmployeeByIdTest_ShouldReturnNotFoundException_WhenEmployeeDoesNotExist() throws Exception {
        when(employeeService.getEmployeeById(1l)).thenThrow(new EmployeeNotFoundException(1l));

        mockMvc.perform(get("/api/employees/{id}", 1l).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(employeeService, times(1)).getEmployeeById(1l);
    }




    @Test
    @DisplayName("This test also should return not found exception when we try delete user which have id = 1")
    void deleteEmployeeByIdTest_ShouldReturnNotFoundException_WhenEmployeeDoesNotExist() throws Exception {

        doThrow(new EmployeeNotFoundException(1l))
                .when(employeeService).deleteEmployee(1l);

        mockMvc.perform(delete("/api/employees/{id}", 1l))
                .andExpect(status().isNotFound());

        Mockito.verify(employeeService, times(1)).deleteEmployee(1l);
    }

}
