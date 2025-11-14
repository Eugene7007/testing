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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
    private EmployeeRepository repository;

    private Employee employee;

    @BeforeEach
    public void setup() {
        employee = new Employee();
        employee.setName("John");
        employee.setDepartment(new Department(1L, "IT", "LA"));
        employee.setPosition("Project Manager");
        employee.setSalary(1111L);
        employee.setManager(null);

    }

    @Test
    void save_ShouldSaveEmployee() {
        var newEmployee = repository.save(employee);

        assertThat(newEmployee.getId()).isNotNull();
        assertThat(newEmployee.getName()).isEqualTo("John");
        assertThat(employee.getDepartment().getId()).isEqualTo(1L);
    }

    @Test
    void save_ShouldThrowException() {
        employee.setName("");
        employee.setPosition("");
        employee.setSalary(null);
        employee.setDepartment(null);
        employee.setManager(null);

        assertThatThrownBy(() -> repository.saveAndFlush(employee))
            .isInstanceOf(Exception.class);
    }

    @Test
    void findById_ShouldReturnSavedEmployee() {
        var savedEmployee = repository.save(employee);

        Optional<Employee> found = repository.findById(savedEmployee.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John");
    }

    @Test
    void findById_ShouldReturnEmptyOptional() {
        Long employeeId = 5L;

        Optional<Employee> found = repository.findById(employeeId);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllEmployees() {
        repository.save(employee);

        List<Employee> employees = repository.findAll();

        assertThat(employees).isNotNull();
        assertThat(employees.size()).isEqualTo(1);
    }

    @Test
    void findAll_ShouldReturnEmptyList() {
        List<Employee> employees = repository.findAll();

        assertThat(employees).isNotNull();
        assertThat(employees).isEmpty();
    }

    @Test
    void deleteById_ShouldDeleteEmployee() {
        repository.save(employee);
        repository.deleteById(employee.getId());
        Optional<Employee> employeeById = repository.findById(employee.getId());

        assertThat(employeeById.isPresent()).isFalse();
    }

    @Test
    void updateById_ShouldUpdateEmployee() {
        var savedEmployee = repository.save(employee);

        savedEmployee.setPosition("Deputy CTO");
        savedEmployee.setSalary(999999L);
        savedEmployee.setManager(null);

        var updatedEmployee = repository.save(savedEmployee);

        assertThat(updatedEmployee.getPosition()).isEqualTo("Deputy CTO");
        assertThat(updatedEmployee.getSalary()).isEqualTo(999999L);
        assertThat(updatedEmployee.getManager()).isNull();
    }

    @Test
    void existsById_ShouldReturnTrue() {
        Employee saved = repository.save(employee);
        boolean isExist = repository.existsById(saved.getId());
        assertThat(isExist).isTrue();
    }


    @Test
    void doesNotExistById_ShouldReturnFalse() {
        var employeeId = 5L;
        boolean isExist = repository.existsById(employeeId);
        assertThat(isExist).isFalse();
    }

    @Test
    void count_ShouldReturnCorrectCountOfEmployees() {
        repository.save(employee);
        List<Employee> list = repository.findAll();
        long count = repository.count();
        assertThat(list.size()).isEqualTo(count);
    }


}
