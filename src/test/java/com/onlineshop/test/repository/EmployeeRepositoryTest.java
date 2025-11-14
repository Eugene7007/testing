package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class EmployeeRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    private Employee employee;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("IT");
        department.setLocation("Tashkent");

        employee = new Employee();
        employee.setName("John Doe");
        employee.setPosition("Developer");
        employee.setSalary(5000L);
        employee.setDepartment(department);
    }

    @Test
    @DisplayName("Save employee - creates employee")
    void testSaveEmployee() {
        Employee savedEmployee = employeeRepository.save(employee);
        assertThat(savedEmployee.getId()).isNotNull();
        assertThat(savedEmployee.getName()).isEqualTo("John Doe");
        assertThat(savedEmployee.getDepartment()).isEqualTo(department);
    }

    @Test
    @DisplayName("Find by ID - returns saved employee")
    void testFindById() {
        Employee savedEmployee = employeeRepository.save(employee);
        Optional<Employee> foundEmployee = employeeRepository.findById(savedEmployee.getId());

        assertThat(foundEmployee).isPresent();
        assertThat(foundEmployee.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Find all - returns list of employees")
    void testFindAll() {
        departmentRepository.save(employee.getDepartment()); // Save department first
        employeeRepository.save(employee);                   // Then save employee

        List<Employee> employees = employeeRepository.findAll();

        assertThat(employees).isNotEmpty();
        assertThat(employees).hasSize(1);
        assertThat(employees.getFirst().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Find by non-existing ID - returns empty")
    void testFindByNonExistingId() {
        Optional<Employee> result = employeeRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find all when repository is empty - returns empty list")
    void testFindAllEmpty() {
        List<Employee> employees = employeeRepository.findAll();

        assertThat(employees).isEmpty();
    }

    @Test
    @DisplayName("Update non-existing employee - should create new entry")
    void testUpdateNonExistingEmployee() {
        employee.setId(12345L);

        Employee saved = employeeRepository.save(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Delete by ID - removes employee")
    void testDeleteById() {
        Employee saved = employeeRepository.save(employee);

        employeeRepository.deleteById(saved.getId());

        Optional<Employee> result = employeeRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Update employee - updates employee fields")
    void testUpdateEmployee() {
        Employee savedEmployee = employeeRepository.save(employee);
        savedEmployee.setSalary(6000L);
        Employee updatedEmployee = employeeRepository.save(savedEmployee);

        assertThat(updatedEmployee.getSalary()).isEqualTo(6000L);
        assertThat(updatedEmployee.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Delete employee - removes employee from repository")
    void testDeleteEmployee() {
        Employee savedEmployee = employeeRepository.save(employee);
        employeeRepository.delete(savedEmployee);

        Optional<Employee> deletedEmployee = employeeRepository.findById(savedEmployee.getId());
        assertThat(deletedEmployee).isNotPresent();
    }
}