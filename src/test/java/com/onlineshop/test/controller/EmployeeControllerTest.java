package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.repository.DepartmentRepository;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Employee Controller Integration Tests")
class EmployeeControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department testDepartment;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setName("IT Department");
        testDepartment = departmentRepository.save(testDepartment);

        testEmployee = new Employee();
        testEmployee.setName("John Doe");
        testEmployee.setPosition("Developer");
        testEmployee.setSalary(50000L);
        testEmployee.setDepartment(testDepartment);
        testEmployee = employeeRepository.save(testEmployee);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/employees - Should return all employees with 200 OK")
    void getAllEmployees_shouldReturnListOfEmployees_whenEmployeesExist() throws Exception {
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("John Doe")))
            .andExpect(jsonPath("$[0].position", is("Developer")))
            .andExpect(jsonPath("$[0].salary", is(50000)));
    }

    @Test
    @DisplayName("GET /api/employees - Should return empty list when no employees exist")
    void getAllEmployees_shouldReturnEmptyList_whenNoEmployeesExist() throws Exception {
        employeeRepository.deleteAll();

        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/employees - Should return multiple employees")
    void getAllEmployees_shouldReturnMultipleEmployees_whenMultipleExist() throws Exception {
        Employee employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setPosition("Manager");
        employee2.setSalary(60000L);
        employee2.setDepartment(testDepartment);
        employeeRepository.save(employee2);

        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("John Doe")))
            .andExpect(jsonPath("$[1].name", is("Jane Smith")));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Should return 404 when ID does not exist")
    void getEmployeeById_shouldReturn404_whenIdDoesNotExist() throws Exception {
        Long nonExistentId = 9999L;

        mockMvc.perform(get("/api/employees/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvSource({"999", "1000", "5000"})
    @DisplayName("GET /api/employees/{id} - Should return 404 for multiple non-existent IDs")
    void getEmployeeById_shouldReturn404_forMultipleNonExistentIds(Long employeeId) throws Exception {
        mockMvc.perform(get("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/employees - Should create employee with valid data")
    void createEmployee_shouldCreateEmployee_whenValidData() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Alice Johnson");
        request.setPosition("Designer");
        request.setSalary(45000L);
        request.setDepartmentId(testDepartment.getId());
        request.setManagerId(testEmployee.getId());

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Alice Johnson")))
            .andExpect(jsonPath("$.position", is("Designer")))
            .andExpect(jsonPath("$.salary", is(45000)))
            .andExpect(jsonPath("$.id", notNullValue()));

        assertThat(employeeRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("POST /api/employees - Should return 400 for invalid name (empty)")
    void createEmployee_shouldReturn400_whenNameIsEmpty() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("");
        request.setPosition("Developer");
        request.setSalary(50000L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(employeeRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/employees - Should return 400 for negative salary")
    void createEmployee_shouldReturn400_whenSalaryIsNegative() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Bob Smith");
        request.setPosition("Developer");
        request.setSalary(-1000L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        assertThat(employeeRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/employees - Should create employee without manager")
    void createEmployee_shouldCreateEmployee_whenManagerIsNull() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Charlie Brown");
        request.setPosition("CEO");
        request.setSalary(100000L);
        request.setDepartmentId(testDepartment.getId());
        request.setManagerId(null);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Charlie Brown")))
            .andExpect(jsonPath("$.managerName", anyOf(nullValue(), is("Нет менеджера"))));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Should update employee when ID exists")
    void updateEmployee_shouldUpdateEmployee_whenIdExists() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("John Updated");
        request.setPosition("Senior Developer");
        request.setSalary(70000L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(put("/api/employees/{id}", testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(testEmployee.getId().intValue())))
            .andExpect(jsonPath("$.name", is("John Updated")))
            .andExpect(jsonPath("$.position", is("Senior Developer")))
            .andExpect(jsonPath("$.salary", is(70000)));

        Employee updated = employeeRepository.findById(testEmployee.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("John Updated");
        assertThat(updated.getPosition()).isEqualTo("Senior Developer");
        assertThat(updated.getSalary()).isEqualTo(70000L);
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Should return 404 when updating non-existent employee")
    void updateEmployee_shouldReturn404_whenIdDoesNotExist() throws Exception {
        Long nonExistentId = 9999L;
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Ghost Employee");
        request.setPosition("Developer");
        request.setSalary(50000L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(put("/api/employees/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Should return 400 for invalid update data")
    void updateEmployee_shouldReturn400_whenInvalidData() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("");
        request.setPosition("");
        request.setSalary(-500L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(put("/api/employees/{id}", testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        Employee unchanged = employeeRepository.findById(testEmployee.getId()).orElseThrow();
        assertThat(unchanged.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Should delete employee when ID exists")
    void deleteEmployee_shouldDeleteEmployee_whenIdExists() throws Exception {
        Long employeeId = testEmployee.getId();

        mockMvc.perform(delete("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        assertThat(employeeRepository.findById(employeeId)).isEmpty();
        assertThat(employeeRepository.findAll()).hasSize(0);
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Should return 404 when deleting non-existent employee")
    void deleteEmployee_shouldReturn404_whenIdDoesNotExist() throws Exception {
        Long nonExistentId = 9999L;

        mockMvc.perform(delete("/api/employees/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        assertThat(employeeRepository.findAll()).hasSize(1);
    }

    @ParameterizedTest
    @CsvSource({"777", "888", "999"})
    @DisplayName("DELETE /api/employees/{id} - Should return 404 for multiple non-existent IDs")
    void deleteEmployee_shouldReturn404_forMultipleNonExistentIds(Long employeeId) throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration flow - Create, Read, Update, Delete employee")
    void completeEmployeeLifecycle_shouldWorkCorrectly() throws Exception {
        EmployeeRequest createRequest = new EmployeeRequest();
        createRequest.setName("Test User");
        createRequest.setPosition("Tester");
        createRequest.setSalary(40000L);
        createRequest.setDepartmentId(testDepartment.getId());

        String createResponse = mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Test User")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        EmployeeResponse created = objectMapper.readValue(createResponse, EmployeeResponse.class);
        Long employeeId = created.id();

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Test User")));

        EmployeeRequest updateRequest = new EmployeeRequest();
        updateRequest.setName("Test User Updated");
        updateRequest.setPosition("Senior Tester");
        updateRequest.setSalary(55000L);
        updateRequest.setDepartmentId(testDepartment.getId());

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Test User Updated")));

        mockMvc.perform(delete("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
