package com.onlineshop.test.repository;

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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class EmployeeRepositoryTest {

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

    private Employee baseEmployee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();

        baseEmployee = new Employee();
        baseEmployee.setName("Mudrick");
        baseEmployee.setSalary(50L);
        baseEmployee.setPosition("Intern");

        baseEmployee = employeeRepository.save(baseEmployee);
    }



    @Test
    void getAllEmployeesBySalaryTest() {
        Employee baseEmployee1 = new Employee();
        baseEmployee1.setName("Mudrick01");
        baseEmployee1.setSalary(505L);
        baseEmployee1.setPosition("Intern");

        employeeRepository.save(baseEmployee1);


        Employee baseEmployee2 = new Employee();
        baseEmployee2.setName("Mudrick2");
        baseEmployee2.setSalary(505L);
        baseEmployee2.setPosition("Developer");

        employeeRepository.save(baseEmployee2);


        Employee baseEmployee3 = new Employee();
        baseEmployee3.setName("Mudrick3");
        baseEmployee3.setSalary(0L);
        baseEmployee3.setPosition("FinanceManager");

        employeeRepository.save(baseEmployee3);


        Employee baseEmployee4 = new Employee();
        baseEmployee4.setName("Mudrick4");
        baseEmployee4.setSalary(505L);
        baseEmployee4.setPosition("EconomyManager");

        employeeRepository.save(baseEmployee4);


        List<Employee> bySalary = employeeRepository.findBySalary(505L);
        assertEquals(3, bySalary.size());
        assertEquals(505L, bySalary.get(0).getSalary());
        assertEquals(505L, bySalary.get(1).getSalary());
        assertEquals(505L, bySalary.get(2).getSalary());
    }

    @Test
    void deleteById() {
        employeeRepository.deleteById(baseEmployee.getId());

        Employee byName = employeeRepository.findByName(baseEmployee.getName());
        assertNull(byName);
    }


    @Test
    void findAll() {
        List<Employee> employees = employeeRepository.findAll();
        assertNotNull(employees);
        assertEquals(1, employees.size());
    }

    @Test
    void findByName(){
        Employee employee = employeeRepository.findByName(baseEmployee.getName());
        assertNotNull(employee);
        assertEquals(baseEmployee.getName(), employee.getName());
    }

    @Test
    void testFindById() {
        Employee employee = employeeRepository.findById(baseEmployee.getId()).orElse(null);
        assertNotNull(employee);
        assertEquals(baseEmployee.getId(), employee.getId());
        assertEquals("Mudrick", employee.getName());
    }

    @Test
    void testSave() {
        Employee newEmployee = new Employee();
        newEmployee.setName("Alice");
        newEmployee.setSalary(100L);
        newEmployee.setPosition("Worker");

        Employee savedEmployee = employeeRepository.save(newEmployee);
        assertNotNull(savedEmployee.getId());
        assertEquals("Alice", savedEmployee.getName());
    }
}
