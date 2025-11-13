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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@Testcontainers
public class EmployeeRepositoryTest {

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
    public void setUp(){
        employee = new Employee();
        employee.setName("Bob");
        employee.setPosition("Engineer");
        employee.setSalary(1_000L);

        department = new Department();
        department.setName("IT Tech");
        department.setLocation("Silicon walley");

        employee.setDepartment(department);

    }

    @Test
    @DisplayName("saveEmployee when all fields are valid")
    public void saveEmployee_WhenAllFieldsAreValid(){
        var savedEmployee = employeeRepository.save(employee);

        assertNotNull(savedEmployee);
        assertNotNull(savedEmployee.getId());
        assertEquals("Bob", savedEmployee.getName());
        assertEquals("Engineer", savedEmployee.getPosition());
        assertEquals(1_000L, savedEmployee.getSalary());
        assertNotNull(savedEmployee.getDepartment());
        assertNotNull(savedEmployee.getDepartment().getId());
        assertEquals("IT Tech", savedEmployee.getDepartment().getName());
        assertEquals("Silicon walley", savedEmployee.getDepartment().getLocation());
    }

    @Test
    @DisplayName("saveEmployee when department is null")
    public void saveEmployee_WhenDepartmentIsNull(){
        employee.setDepartment(null);

        var savedEmployee = employeeRepository.save(employee);

        assertNotNull(savedEmployee);
        assertNull(employee.getDepartment());
    }

    @Test
    @DisplayName("findById when employee is valid")
    public void findById_WhenEmployeeIsValid(){
        var savedEmployee = employeeRepository.save(employee);

        Optional<Employee> foundEmployee = employeeRepository.findById(savedEmployee.getId());

        assertThat(foundEmployee).isPresent();
        assertThat(foundEmployee.get().getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("findById when employee does not exist")
    public void findById_WhenEmployeeDoesNotExist(){
        Optional<Employee> foundEmployee = employeeRepository.findById(123L);

        assertThat(foundEmployee).isEmpty();
    }

    @Test
    @DisplayName("findAll saved employees")
    public void findAll() {
        var savedEmployee1 = employeeRepository.save(employee);

        var employee2 = new Employee();
        employee2.setName("John");
        employee2.setSalary(1_200L);
        employee.setPosition("Engineer");
        employee2.setDepartment(department);

        var savedEmployee2 = employeeRepository.save(employee2);

        List<Employee> foundEmployees = employeeRepository.findAll();

        assertThat(foundEmployees).hasSize(2);
        assertThat(foundEmployees).contains(savedEmployee1, savedEmployee2);
        assertThat(foundEmployees).extracting(Employee::getName)
            .containsExactlyInAnyOrder("Bob", "John");
    }

    @Test
    @DisplayName("findAll when employees do not exist")
    public void findAll_WhenEmployeesDoNotExist(){

        List<Employee> foundEmployees = employeeRepository.findAll();

        assertThat(foundEmployees).isEmpty();
    }

    @Test
    @DisplayName("updateEmployee when employee is valid")
    public void updateEmployee_WhenEmployeeIsValid(){
        var savedEmployee = employeeRepository.save(employee);

        savedEmployee.setName("Jacob");
        savedEmployee.setSalary(3_000L);
        savedEmployee.setPosition("Main Engineer");

        var updatedEmployee = employeeRepository.save(savedEmployee);

        assertEquals(savedEmployee.getId(), updatedEmployee.getId());
        assertEquals("Jacob", updatedEmployee.getName());
        assertEquals(3_000L, updatedEmployee.getSalary());
        assertEquals("Main Engineer", updatedEmployee.getDepartment().getName());
    }

    @Test
    @DisplayName("updateEmployee when employee is invalid")
    public void updateEmployee_WhenEmployeeIsInvalid(){
        var savedEmployee = employeeRepository.save(employee);

        savedEmployee.setName("Jacob");
        savedEmployee.setSalary(3_000L);

        var updatedEmployee = employeeRepository.save(savedEmployee);

        assertEquals("Bob", updatedEmployee.getName());
        assertEquals(2_000L, updatedEmployee.getSalary());
    }

    @Test
    @DisplayName("deleteEmployee when employee is valid")
    public void deleteEmployee_WhenEmployeeIsValid(){
        var savedEmployee = employeeRepository.save(employee);
        Long employeeId = savedEmployee.getId();

        employeeRepository.deleteById(employeeId);

        Optional<Employee> deletedEmployee = employeeRepository.findById(employeeId);
        assertThat(deletedEmployee).isEmpty();
    }

    @Test
    @DisplayName("existsEmployee when employee is valid")
    public void existsEmployee_WhenEmployeeIsValid(){
        var savedEmployee = employeeRepository.save(employee);

        boolean exists = employeeRepository.existsById(savedEmployee.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsEmployee when employee is invalid")
    public void existsEmployee_WhenEmployeeIsInvalid(){
        boolean exists = employeeRepository.existsById(123L);

        assertThat(exists).isFalse();
    }
}