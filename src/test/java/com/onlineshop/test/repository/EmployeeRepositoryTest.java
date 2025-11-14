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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    private Employee employee;

    @BeforeEach
    void setUpEmployeeParams() {
        employee = new Employee();
        employee.setName("Sanjar");
        employee.setPosition("Java Engineer");
        employee.setSalary(100L);
    }

    @Test
    @DisplayName("Save employee to base")
    void testSaveEmployee() {
        Employee saved = employeeRepository.save(employee);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Sanjar");
    }

    @Test
    @DisplayName("Get employee by ID")
    void testFindById() {
        Employee saved = employeeRepository.save(employee);
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Sanjar");
    }

    @Test
    @DisplayName("Return empty object if didn't find")
    void testFindByIdNotFound() {
        Optional<Employee> found = employeeRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Checking for not null in saved employees")
    void testFindAll() {
        employeeRepository.save(new Employee(null, "Sanjar", "Java", 100L, null, null));
        employeeRepository.save(new Employee(null, "Anvar", "Java", 150L, null, null));

        List<Employee> employees = employeeRepository.findAll();

        assertThat(employees).isNotNull();
    }

    @Test
    @DisplayName("Update employee")
    void testUpdateEmployee() {
        Employee saved = employeeRepository.save(employee);
        saved.setName("Updated");
        saved.setSalary(999L);
        Employee updated = employeeRepository.save(saved);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getSalary()).isEqualTo(999L);
    }

    @Test
    @DisplayName("Delete employee by ID")
    void testDeleteById() {
        Employee saved = employeeRepository.save(employee);
        employeeRepository.deleteById(saved.getId());

        assertThat(employeeRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("Delete all employees")
    void testDeleteAll() {
        employeeRepository.save(new Employee(null, "Sanjar", "Java", 1000L, null, null));
        employeeRepository.save(new Employee(null, "Sardor", "Dev", 2000L, null, null));

        employeeRepository.deleteAll();

        assertThat(employeeRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Save employee with department")
    void testSaveEmployeeWithDepartment() {
        Department department = new Department();
        department.setName("IT");

        Employee emp = new Employee(null, "Sanjar", "QA", 100L, department, null);
        Employee saved = employeeRepository.save(emp);

        assertThat(saved.getDepartment()).isNotNull();
        assertThat(saved.getDepartment().getName()).isEqualTo("IT");
    }

    @Test
    @DisplayName("Save employee with manager")
    void testSaveEmployeeWithManager() {
        Employee manager = new Employee(null, "Eugene", "Team lead", 10000L, null, null);
        Employee worker = new Employee(null, "Sanjar", "Junior Java developer", 100L, null, manager);

        employeeRepository.save(manager);
        Employee savedWorker = employeeRepository.save(worker);

        assertThat(savedWorker.getManager()).isNotNull();
        assertThat(savedWorker.getManager().getName()).isEqualTo("Eugene");
    }
    @Test
    @DisplayName("Save some employees and check them")
    void testSaveMultipleEmployees() {
        Employee e1 = new Employee(null, "Sanjar", "QA", 1000L, null, null);
        Employee e2 = new Employee(null, "Dmitrii", "Dev", 2000L, null, null);

        employeeRepository.saveAll(List.of(e1, e2));

        List<Employee> all = employeeRepository.findAll();
        assertThat(all).isNotNull();
    }

}
