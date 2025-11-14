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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Employee employee;

    private Department department;

    @BeforeEach
    public void setUp() {
        department = new Department();
        department.setName("Workers");
        department.setLocation("Moscow");

        employee = new Employee();
        employee.setName("Mary");
        employee.setPosition("Developer");
        employee.setSalary(150000L);
        employee.setDepartment(department);
    }

    @Test
    @DisplayName("Saving employee happy flow")
    void saveEmployee_ShouldSaveWithoutMistakes() {
        Employee saveEmployee = employeeRepository.save(employee);
        assertThat(saveEmployee.getId()).isNotNull();
        assertThat(saveEmployee.getName()).isEqualTo("Mary");
    }

    @Test
    @DisplayName("Savie employee - Should Not Save When Name is null")
    void saveEmployee_ShouldNotSaveWhenNameIsNull() {
        employee.setName(null);
        assertThatThrownBy(() -> employeeRepository.save(employee))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Find Employee by ID happy flow")
    void findEmployeeById_ShouldFindEmployeeById() {
        Employee saveEmployee = employeeRepository.save(employee);
        Optional<Employee> foundEmployee = employeeRepository.findById(saveEmployee.getId());

        assertThat(foundEmployee.isPresent());
        assertThat(foundEmployee.get().getName()).isEqualTo("Mary");
        assertThat(foundEmployee.get().getPosition()).isEqualTo("Developer");
    }

    @Test
    @DisplayName("Find Employee By Id - Should return empty when Employee does not exist")
    void findEmployeeById_ShouldReturnEmptyOptionalWhenEmployeeDoesNotExist() {
        Optional<Employee> foundEmployee = employeeRepository.findById(1L);
        assertThat(foundEmployee.isEmpty());
    }

    @Test
    @DisplayName("Count employees - Should return number of employees")
    void countEmployees_ShouldReturnNumberOfEmployees() {
        var employee1 = new Employee();
        employee1.setName("Nick");
        employee1.setSalary(200000L);
        employee1.setDepartment(department);
        employeeRepository.save(employee);
        employeeRepository.save(employee1);
        long num = employeeRepository.count();
        assertThat(num).isEqualTo(2);
    }

    @Test
    @DisplayName("Delete employee - Should remove Employee")
    void deleteEmployee_ShouldRemoveEmployee() {
        Employee savedEmp = employeeRepository.save(employee);
        employeeRepository.deleteById(savedEmp.getId());
        employee.setId(1L);
        employeeRepository.deleteById(1L);

        Optional<Employee> foundEmployee = employeeRepository.findById(employee.getId());
        assertThat(foundEmployee.isEmpty());
    }

    @Test
    @DisplayName("Find all — should return empty list when no employees exist")
    void findAll_ShouldReturnEmptyList_WhenNoEmployees() {
        employeeRepository.deleteAll();
        var result = employeeRepository.findAll();
        assertThat(result).isEmpty();
    }
}