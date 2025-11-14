package com.onlineshop.test;

import com.onlineshop.test.controller.EmployeeController;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.repository.EmployeeRepository;
import com.onlineshop.test.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTests {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    private EmployeeResponse response_1 = new EmployeeResponse(1L, "Lebron", "SF", 10L, "Lakers", "JJ Redick");
    private EmployeeResponse response_2 = new EmployeeResponse(2L, "Luka", "PG", 5L, "Lakers", "JJ Redick");
    private EmployeeResponse response_3 = new EmployeeResponse(3L, "Shai", "PG", 3L, "Thunders", "Someone");


    @Test
    public void getAllEmployees_ReturnEmployeeList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(response_1, response_2));

        mockMvc.perform(get("/api/employees")).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(2)).andExpect(jsonPath("$[0].id").value(1L)).andExpect(jsonPath("$[0].name").value("Lebron")).andExpect(jsonPath("$[0].position").value("SF")).andExpect(jsonPath("$[1].id").value(2L)).andExpect(jsonPath("$[1].name").value("Luka")).andExpect(jsonPath("$[1].position").value("PG"));
    }

    @Test
    public void getAllEmployees_ReturnEmptyList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employees")).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(0));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void getEmployeeById_ReturnEmployee() throws Exception {
        long id = 1L;

        when(employeeService.getEmployeeById(id)).thenReturn(response_1);

        mockMvc.perform(get("/api/employees/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("Lebron")).andExpect(jsonPath("$.position").value("SF"));
    }

    @Test
    public void getEmployeeById_ThrowException() throws Exception {
        long id = 3;

        when(employeeService.getEmployeeById(id)).thenThrow(new EmployeeNotFoundException(id));

        mockMvc.perform(get("/api/employees/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    public void createEmployee_ReturnCreatedEmployee() throws Exception {

        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(response_3);

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("""
            {
                "name": "Shai",
                "position": "PG",
                "salary": 3,
                "departmentId": 1
            }
            """)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(3L)).andExpect(jsonPath("$.name").value("Shai")).andExpect(jsonPath("$.position").value("PG"));
    }

    @Test
    public void createEmployee_ReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "name": "",
                    "position": "PG",
                    "salary": 3,
                    "departmentId": 1
                }
            """)).andExpect(status().isBadRequest());
    }

    @Test
    public void updateEmployee_UpdateAndReturnEmployee() throws Exception {
        long id = 2L;
        EmployeeResponse response = new EmployeeResponse(2L, "Carmelo", "SF", 7L, "Nuggets", "Someone");


        when(employeeService.updateEmployee(eq(id), any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
            {
                "name" : "Carmelo",
                "position" : "SF",
                "salary" : 7,
                "departmentId": 1
            }
            """)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(2L)).andExpect(jsonPath("$.name").value("Carmelo")).andExpect(jsonPath("$.position").value("SF"));
    }

    @Test
    public void updateEmployee_ThrowException() throws Exception {
        long id = 4L;

        when(employeeService.updateEmployee(eq(id), any(EmployeeRequest.class))).thenThrow(new EmployeeNotFoundException(id));

        mockMvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
            {
                "name" : "Carmelo",
                "position" : "SF",
                "salary" : 7,
                "departmentId": 1
            }
            """)).andExpect(status().isNotFound());
    }

    @Test
    public void updateEmployee_InvalidJson() throws Exception {
        mockMvc.perform(put("/api/employees/2").contentType(MediaType.APPLICATION_JSON).content("{ Something }")).andExpect(status().isInternalServerError());
    }

    @Test
    public void deleteEmployee_DeleteEmployee() throws Exception {
        Employee employee = new Employee();
        long id = 3L;
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        mockMvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isOk());

        verify(employeeService, times(1)).deleteEmployee(id);
    }

    @Test
    public void deleteEmployee_ThrowException() throws Exception {
        long id = 4L;

        doThrow(new EmployeeNotFoundException(id)).when(employeeService).deleteEmployee(id);


        mockMvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isNotFound());

        verify(employeeService, times(1)).deleteEmployee(id);
    }
}