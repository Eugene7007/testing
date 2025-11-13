package com.onlineshop.test.controller;

import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnAllEmployeesWhenExist() throws Exception {
        Employee e1 = new Employee(null, "Rustam", "Backend", BigDecimal.valueOf(50000));
        Employee e2 = new Employee(null, "Diyor", "Frontend", BigDecimal.valueOf(45000));
        employeeRepository.save(e1);
        employeeRepository.save(e2);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Rustam"))
                .andExpect(jsonPath("$[1].name").value("Diyor"));
    }

    @Test
    void shouldGetEmployeeByIdWhenExists() throws Exception {
        Employee emp = new Employee(null, "Shox", "Manager", BigDecimal.valueOf(70000));
        Employee saved = employeeRepository.save(emp);

        mockMvc.perform(get("/api/employees/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Shox"))
                .andExpect(jsonPath("$.position").value("Manager"));
    }

    @Test
    void shouldReturn404WhenGetByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateEmployeeSuccessfully() throws Exception {
        String json = """
        {
            "name": "Kamola",
            "position": "HR",
            "salary": 30000
        }
        """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kamola"))
                .andExpect(jsonPath("$.position").value("HR"))
                .andExpect(jsonPath("$.salary").value(30000));
    }

    @Test
    void shouldReturn400WhenCreateWithEmptyName() throws Exception {
        String json = """
        {
            "name": "",
            "position": "Intern",
            "salary": 10000
        }
        """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateEmployeeWhenExists() throws Exception {
        Employee emp = new Employee(null, "Old", "Old Pos", BigDecimal.valueOf(100));
        Employee saved = employeeRepository.save(emp);

        String updateJson = """
        {
            "name": "Updated Name",
            "position": "Senior Dev",
            "salary": 90000
        }
        """;

        mockMvc.perform(put("/api/employees/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.salary").value(90000));
    }

    @Test
    void shouldReturn404WhenUpdateNotFound() throws Exception {
        String json = """
        {
            "name": "Ghost",
            "position": "Ghost",
            "salary": 0
        }
        """;

        mockMvc.perform(put("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteEmployeeWhenExists() throws Exception {
        Employee emp = new Employee(null, "Delete Me", "Temp", BigDecimal.valueOf(1));
        Employee saved = employeeRepository.save(emp);

        mockMvc.perform(delete("/api/employees/" + saved.getId()))
                .and.andExpect(status().isOk());

        assertFalse(employeeRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldReturn404WhenDeleteNotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateAndThenFindInList() throws Exception {
        String json = """
        {
            "name": "Aziz",
            "position": "Designer",
            "salary": 40000
        }
        """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aziz"));
    }

    @Test
    void shouldUpdateAndSeeChangesInGet() throws Exception {
        Employee emp = new Employee(null, "Temp", "Temp", BigDecimal.valueOf(1));
        Employee saved = employeeRepository.save(emp);

        String updateJson = """
        {
            "name": "Final Name",
            "position": "Lead",
            "salary": 100000
        }
        """;

        mockMvc.perform(put("/api/employees/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Final Name"))
                .andExpect(jsonPath("$.salary").value(100000));
    }

    @Test
    void shouldReturn400WhenSalaryIsNull() throws Exception {
        String json = """
        {
            "name": "No Salary",
            "position": "Intern",
            "salary": null
        }
        """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}