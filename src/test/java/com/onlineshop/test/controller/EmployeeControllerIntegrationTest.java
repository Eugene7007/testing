package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.repository.DepartmentRepository;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Integration tests for EmployeeController")
public class EmployeeControllerIntegrationTest {

    @Container
    @SuppressWarnings("resource")
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

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setName("IT Department");
        testDepartment.setLocation("Moscow");
        testDepartment = departmentRepository.save(testDepartment);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    // Тест 1: GET всех сотрудников - пустой список
    @Test
    @DisplayName("GET /api/employees - should return empty list when no employees exist")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExist() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // AssertJ проверка базы данных
        assertThat(employeeRepository.findAll()).isEmpty();
    }

    // Тест 2: POST создание сотрудника
    @Test
    @DisplayName("POST /api/employees - should create employee successfully")
    void createEmployee_ShouldReturnCreatedEmployee_WhenValidRequest() throws Exception {
        var request = new EmployeeRequest();
        request.setName("Иван Иванов");
        request.setPosition("Software Developer");
        request.setSalary(100000L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Иван Иванов")))
                .andExpect(jsonPath("$.position", is("Software Developer")))
                .andExpect(jsonPath("$.salary", is(100000)))
                .andExpect(jsonPath("$.departmentName", is("IT Department")));

        // AssertJ проверки базы данных
        var employees = employeeRepository.findAll();
        assertThat(employees).hasSize(1);
        assertThat(employees.getFirst().getName()).isEqualTo("Иван Иванов");
        assertThat(employees.getFirst().getPosition()).isEqualTo("Software Developer");
        assertThat(employees.getFirst().getSalary()).isEqualTo(100000L);
    }

    // Тест 3: GET сотрудника по ID
    @Test
    @DisplayName("GET /api/employees/{id} - should return employee when exists")
    void getEmployeeById_ShouldReturnEmployee_WhenEmployeeExists() throws Exception {
        var employee = new Employee();
        employee.setName("Петр Петров");
        employee.setPosition("QA Engineer");
        employee.setSalary(80000L);
        employee.setDepartment(testDepartment);
        employee = employeeRepository.save(employee);

        mockMvc.perform(get("/api/employees/" + employee.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employee.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Петр Петров")))
                .andExpect(jsonPath("$.position", is("QA Engineer")))
                .andExpect(jsonPath("$.salary", is(80000)));

        // AssertJ проверка
        var foundEmployee = employeeRepository.findById(employee.getId());
        assertThat(foundEmployee).isPresent();
        assertThat(foundEmployee.get().getName()).isEqualTo("Петр Петров");
    }

    // Тест 4: GET всех сотрудников с данными
    @Test
    @DisplayName("GET /api/employees - should return list of employees when employees exist")
    void getAllEmployees_ShouldReturnEmployeesList_WhenEmployeesExist() throws Exception {
        var employee1 = new Employee();
        employee1.setName("Анна Смирнова");
        employee1.setPosition("Designer");
        employee1.setSalary(75000L);
        employee1.setDepartment(testDepartment);

        var employee2 = new Employee();
        employee2.setName("Сергей Кузнецов");
        employee2.setPosition("DevOps");
        employee2.setSalary(95000L);
        employee2.setDepartment(testDepartment);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Анна Смирнова")))
                .andExpect(jsonPath("$[1].name", is("Сергей Кузнецов")));

        // AssertJ проверка
        var allEmployees = employeeRepository.findAll();
        assertThat(allEmployees)
                .hasSize(2)
                .extracting(Employee::getName)
                .containsExactly("Анна Смирнова", "Сергей Кузнецов");
    }

    // Тест 5: PUT обновление сотрудника
    @Test
    @DisplayName("PUT /api/employees/{id} - should update employee successfully")
    void updateEmployee_ShouldReturnUpdatedEmployee_WhenEmployeeExists() throws Exception {
        var employee = new Employee();
        employee.setName("Мария Сидорова");
        employee.setPosition("Junior Developer");
        employee.setSalary(60000L);
        employee.setDepartment(testDepartment);
        employee = employeeRepository.save(employee);

        var updateRequest = new EmployeeRequest();
        updateRequest.setName("Мария Сидорова");
        updateRequest.setPosition("Middle Developer");
        updateRequest.setSalary(85000L);
        updateRequest.setDepartmentId(testDepartment.getId());

        mockMvc.perform(put("/api/employees/" + employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employee.getId().intValue())))
                .andExpect(jsonPath("$.position", is("Middle Developer")))
                .andExpect(jsonPath("$.salary", is(85000)));

        // AssertJ проверка обновления в БД
        var updatedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(updatedEmployee.getPosition()).isEqualTo("Middle Developer");
        assertThat(updatedEmployee.getSalary()).isEqualTo(85000L);
    }

    // Тест 6: DELETE удаление сотрудника
    @Test
    @DisplayName("DELETE /api/employees/{id} - should delete employee successfully")
    void deleteEmployee_ShouldDeleteEmployee_WhenEmployeeExists() throws Exception {
        var employee = new Employee();
        employee.setName("Дмитрий Волков");
        employee.setPosition("Manager");
        employee.setSalary(90000L);
        employee.setDepartment(testDepartment);
        employee = employeeRepository.save(employee);

        Long employeeId = employee.getId();

        mockMvc.perform(delete("/api/employees/" + employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // AssertJ проверка удаления из БД
        assertThat(employeeRepository.findById(employeeId)).isEmpty();
        assertThat(employeeRepository.existsById(employeeId)).isFalse();
    }

    // Тест 7: GET несуществующего сотрудника - 404
    @Test
    @DisplayName("GET /api/employees/{id} - should return 404 when employee does not exist")
    void getEmployeeById_ShouldReturn404_WhenEmployeeDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/employees/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // AssertJ проверка
        assertThat(employeeRepository.findById(99999L)).isEmpty();
    }

    // Тест 8: POST с пустым именем - валидация
    @Test
    @DisplayName("POST /api/employees - should return 400 when name is empty")
    void createEmployee_ShouldReturn400_WhenNameIsEmpty() throws Exception {
        var request = new EmployeeRequest();
        request.setName("");
        request.setPosition("Developer");
        request.setSalary(100000L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // AssertJ проверка что сотрудник не создан
        assertThat(employeeRepository.findAll()).isEmpty();
    }

    // Тест 9: POST с отрицательной зарплатой - валидация
    @Test
    @DisplayName("POST /api/employees - should return 400 when salary is negative")
    void createEmployee_ShouldReturn400_WhenSalaryIsNegative() throws Exception {
        var request = new EmployeeRequest();
        request.setName("Иван Иванов");
        request.setPosition("Developer");
        request.setSalary(-1000L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // AssertJ проверка что сотрудник не создан
        assertThat(employeeRepository.count()).isZero();
    }

    // Тест 10: POST с null departmentId - валидация
    @Test
    @DisplayName("POST /api/employees - should return 400 when departmentId is null")
    void createEmployee_ShouldReturn400_WhenDepartmentIdIsNull() throws Exception {
        var request = new EmployeeRequest();
        request.setName("Иван Иванов");
        request.setPosition("Developer");
        request.setSalary(100000L);
        request.setDepartmentId(null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Тест 11: PUT несуществующего сотрудника - 404
    @Test
    @DisplayName("PUT /api/employees/{id} - should return 404 when employee does not exist")
    void updateEmployee_ShouldReturn404_WhenEmployeeDoesNotExist() throws Exception {
        var request = new EmployeeRequest();
        request.setName("Иван Иванов");
        request.setPosition("Developer");
        request.setSalary(100000L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(put("/api/employees/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // Тест 12: PUT с невалидными данными
    @Test
    @DisplayName("PUT /api/employees/{id} - should return 400 when update data is invalid")
    void updateEmployee_ShouldReturn400_WhenDataIsInvalid() throws Exception {
        var employee = new Employee();
        employee.setName("Елена Павлова");
        employee.setPosition("Analyst");
        employee.setSalary(70000L);
        employee.setDepartment(testDepartment);
        employee = employeeRepository.save(employee);

        var request = new EmployeeRequest();
        request.setName("Елена Павлова");
        request.setPosition("");  // Пустая должность
        request.setSalary(70000L);
        request.setDepartmentId(testDepartment.getId());

        mockMvc.perform(put("/api/employees/" + employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // AssertJ проверка что данные не изменились
        var unchangedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(unchangedEmployee.getPosition()).isEqualTo("Analyst");
    }

    // Тест 13: DELETE несуществующего сотрудника - 404
    @Test
    @DisplayName("DELETE /api/employees/{id} - should return 404 when employee does not exist")
    void deleteEmployee_ShouldReturn404_WhenEmployeeDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/employees/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Тест 14: POST нескольких сотрудников и GET всех
    @Test
    @DisplayName("POST multiple employees and GET all - should return all created employees")
    void createMultipleEmployees_ShouldReturnAllEmployees_WhenGetAll() throws Exception {
        var request1 = new EmployeeRequest();
        request1.setName("Алексей Новиков");
        request1.setPosition("Backend Developer");
        request1.setSalary(110000L);
        request1.setDepartmentId(testDepartment.getId());

        var request2 = new EmployeeRequest();
        request2.setName("Ольга Белова");
        request2.setPosition("Frontend Developer");
        request2.setSalary(105000L);
        request2.setDepartmentId(testDepartment.getId());

        var request3 = new EmployeeRequest();
        request3.setName("Николай Морозов");
        request3.setPosition("Team Lead");
        request3.setSalary(150000L);
        request3.setDepartmentId(testDepartment.getId());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // AssertJ проверки
        var allEmployees = employeeRepository.findAll();
        assertThat(allEmployees)
                .hasSize(3)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Алексей Новиков", "Ольга Белова", "Николай Морозов");

        assertThat(allEmployees)
                .extracting(Employee::getSalary)
                .allMatch(salary -> salary > 0);
    }

    // Тест 15: PUT полное обновление всех полей
    @Test
    @DisplayName("PUT /api/employees/{id} - should update all fields successfully")
    void updateEmployee_ShouldUpdateAllFields_WhenAllFieldsProvided() throws Exception {
        var employee = new Employee();
        employee.setName("Виктор Соколов");
        employee.setPosition("Junior Tester");
        employee.setSalary(55000L);
        employee.setDepartment(testDepartment);
        employee = employeeRepository.save(employee);

        var newDepartment = new Department();
        newDepartment.setName("QA Department");
        newDepartment.setLocation("Saint Petersburg");
        newDepartment = departmentRepository.save(newDepartment);

        var updateRequest = new EmployeeRequest();
        updateRequest.setName("Виктор Соколов-Старший");
        updateRequest.setPosition("Senior QA Engineer");
        updateRequest.setSalary(120000L);
        updateRequest.setDepartmentId(newDepartment.getId());

        mockMvc.perform(put("/api/employees/" + employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employee.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Виктор Соколов-Старший")))
                .andExpect(jsonPath("$.position", is("Senior QA Engineer")))
                .andExpect(jsonPath("$.salary", is(120000)))
                .andExpect(jsonPath("$.departmentName", is("QA Department")));

        // AssertJ комплексная проверка всех обновленных полей
        var updatedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        Department finalNewDepartment = newDepartment;
        assertThat(updatedEmployee)
                .satisfies(emp -> {
                    assertThat(emp.getName()).isEqualTo("Виктор Соколов-Старший");
                    assertThat(emp.getPosition()).isEqualTo("Senior QA Engineer");
                    assertThat(emp.getSalary()).isEqualTo(120000L);
                    assertThat(emp.getDepartment().getName()).isEqualTo("QA Department");
                    assertThat(emp.getDepartment().getId()).isEqualTo(finalNewDepartment.getId());
                });
    }
}
