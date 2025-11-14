package com.onlineshop.test.repository;


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
public class EmployeeRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = new Employee();
        employee.setName("Mukhammadjon");
        employee.setSalary(100000L);
        employee.setManager(null);
        employee.setDepartment(null);
    }

    @Test
    @DisplayName("Should save employee")
    void save_ShouldSaveEmployee() {
        Employee saved = employeeRepository.save(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Mukhammadjon");
    }

    @Test
    @DisplayName("Should find employee by id")
    void findById_ShouldReturnEmployee() {
        Employee saved = employeeRepository.save(employee);

        Optional<Employee> found = employeeRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Mukhammadjon");
    }

    @Test
    @DisplayName("Should return empty when employee not found")
    void findById_ShouldReturnEmpty() {
        Optional<Employee> found = employeeRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return all employees")
    void findAll_ShouldReturnAllEmployees() {
        employeeRepository.save(employee);

        Employee employee2 = new Employee();
        employee2.setName("Sardor");
        employee2.setPosition("hr");
        employee2.setSalary(12343L);
        employeeRepository.save(employee2);

        List<Employee> employees = employeeRepository.findAll();

        assertThat(employees).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when no employees")
    void findAll_ShouldReturnEmptyList() {
        List<Employee> employees = employeeRepository.findAll();

        assertThat(employees).isEmpty();
    }

    @Test
    @DisplayName("Should update employee")
    void update_ShouldUpdateEmployee() {
        Employee saved = employeeRepository.save(employee);

        saved.setName("Test");
        employeeRepository.save(saved);

        Employee updated = employeeRepository.findById(saved.getId()).get();

        assertThat(updated.getName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Should delete employee by id")
    void deleteById_ShouldRemoveEmployee() {
        Employee saved = employeeRepository.save(employee);

        employeeRepository.deleteById(saved.getId());

        Optional<Employee> deleted = employeeRepository.findById(saved.getId());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should delete employee")
    void delete_ShouldRemoveEmployee() {
        Employee saved = employeeRepository.save(employee);

        employeeRepository.delete(saved);

        Optional<Employee> deleted = employeeRepository.findById(saved.getId());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should return true when employee exists")
    void existsById_ShouldReturnTrue() {
        Employee saved = employeeRepository.save(employee);

        boolean exists = employeeRepository.existsById(saved.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should count all employees")
    void count_ShouldReturnEmployeeCount() {
        employeeRepository.save(employee);

        Employee employee2 = new Employee();
        employee2.setName("Sardor");
        employee2.setPosition("hr");
        employee2.setSalary(12343L);
        employeeRepository.save(employee2);

        long count = employeeRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return false when employee does not exist")
    void existsById_ShouldReturnFalse() {
        boolean exists = employeeRepository.existsById(100L);

        assertThat(exists).isFalse();
    }

}
