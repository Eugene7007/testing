package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Testcontainers
public class EmployeeRepositoryTest {

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
        departmentRepository.save(department);

        employee = new Employee();
        employee.setName("Yuriy");
        employee.setPosition("Engineer");
        employee.setSalary(1500L);
        employee.setDepartment(department);
    }

    @Test
    void testSaveEmployee() {
        Employee saved = employeeRepository.save(employee);
        assertNotNull(saved.getId());
    }

    @Test
    void testFindById() {
        Employee saved = employeeRepository.save(employee);
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Yuriy");
    }

    @Test
    void testFindAll() {
        employeeRepository.save(employee);
        Employee emp2 = new Employee();
        emp2.setName("Anna");
        emp2.setPosition("Data Analyst");
        emp2.setSalary(1600L);
        emp2.setDepartment(department);
        employeeRepository.save(emp2);

        List<Employee> all = employeeRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void testUpdateEmployee() {
        Employee saved = employeeRepository.save(employee);
        saved.setPosition("Senior Engineer");
        employeeRepository.save(saved);

        Optional<Employee> updated = employeeRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getPosition()).isEqualTo("Senior Engineer");
    }

    @Test
    void testDeleteEmployee() {
        Employee saved = employeeRepository.save(employee);
        employeeRepository.deleteById(saved.getId());

        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testDeleteAllEmployees() {
        employeeRepository.save(employee);
        employeeRepository.deleteAll();
        List<Employee> all = employeeRepository.findAll();
        assertEquals(0, all.size());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Employee> found = employeeRepository.findById(999L);
        assertFalse(found.isPresent(), "Employee should not be found");
    }

}
