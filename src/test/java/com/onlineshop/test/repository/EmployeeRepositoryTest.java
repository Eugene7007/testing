package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Customer;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Integration tests
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
    private Employee employee2;

    @BeforeEach
    public void setUp(){
        employee = new Employee();
        employee.setName("John");
        employee.setPosition("Accountant");
        employee.setSalary(250L);

        employee2 = new Employee();
        employee2.setName("Maria");
        employee2.setPosition("Researcher");
        employee2.setSalary(150L);

        employeeRepository.save(employee);
        employeeRepository.save(employee2);

    }

    @Test
    void testSave(){
        Employee savedEmployee = employeeRepository.save(employee);
        assertNotNull(savedEmployee.getId());
    }

    @Test
    void testFindById(){
        assertNotNull(employeeRepository.findById(1L));
    }

    @Test
    void testDeleteById(){
        Long id = employee.getId();
        employeeRepository.deleteById(id);
        assertThat(employeeRepository.findById(id)).isEmpty();
    }

    @Test
    void testExistsById(){
        assertThat(employeeRepository.existsById(employee.getId())).isTrue();
    }

    @Test
    void testFindAll(){
        List<Employee> employeeList = employeeRepository.findAll();
        assertEquals(2, employeeList.size());
    }

    @Test
    void testDeleteAll(){
        employeeRepository.deleteAll();
        assertThat(employeeRepository.findAll()).isEmpty();
    }

    @Test
    void testCount(){
        assertEquals(2, employeeRepository.count());
    }

    @Test
    void testEquals(){
        assertThat(employee.equals(employee2)).isFalse();
    }

    @Test
    void testSaveAll(){
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(employee);
        employeeList.add(employee2);
        employeeRepository.saveAll(employeeList);
        assertEquals(employeeList.size(), employeeRepository.count());
    }


}
