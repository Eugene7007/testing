package com.onlineshop.test;

import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.repository.DepartmentRepository;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
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
public class EmployeeRepositoryTests {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine").withDatabaseName("testdb").withUsername("postgres").withPassword("password");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    private Employee employee_1;
    private Employee employee_2;
    private Employee employee_3;
    private Department salesDept;
    private Employee manager;


    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        // создаём department
        salesDept = new Department();
        salesDept.setName("Sales");
        salesDept = departmentRepository.save(salesDept);

        // создаём менеджера
        manager = new Employee();
        manager.setName("Manager");
        manager.setPosition("MG");
        manager.setSalary(15000L);
        manager = employeeRepository.save(manager);

        // создаём сотрудников через сеттеры
        employee_1 = new Employee();
        employee_1.setName("Lebron");
        employee_1.setPosition("SF");
        employee_1.setSalary(10000L);
        employee_1.setDepartment(salesDept);
        employee_1.setManager(manager);
        employee_1 = employeeRepository.save(employee_1);

        employee_2 = new Employee();
        employee_2.setName("Luka");
        employee_2.setPosition("PG");
        employee_2.setSalary(12000L);
        employee_2.setManager(manager);
        employee_2 = employeeRepository.save(employee_2);

        employee_3 = new Employee();
        employee_3.setName("Shai");
        employee_3.setPosition("PG");
        employee_3.setSalary(9000L);
        employee_3.setDepartment(salesDept);
        employee_3 = employeeRepository.save(employee_3);
    }


    @Test
    public void Save() {
        Employee employee = new Employee();
        employee.setName("Carmelo");
        employee.setPosition("SF");
        employee.setSalary(7L);
        employee.setDepartment(salesDept);
        employee.setManager(manager);

        Employee saved = employeeRepository.save(employee);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Carmelo");
    }

    @Test
    void FindById() {
        Optional<Employee> found = employeeRepository.findById(employee_1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Lebron");
    }

    @Test
    void testFindAll() {
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).hasSize(4) // включая manager
            .extracting(Employee::getName).contains("Lebron", "Luka", "Shai", "Manager");
    }

    @Test
    void ExistsById() {
        assertThat(employeeRepository.existsById(employee_2.getId())).isTrue();
    }

    @Test
    void Delete() {
        employeeRepository.delete(employee_2);
        assertThat(employeeRepository.existsById(employee_2.getId())).isFalse();
    }

    @Test
    void DeleteById() {
        employeeRepository.deleteById(employee_3.getId());
        assertThat(employeeRepository.existsById(employee_3.getId())).isFalse();
    }

    @Test
    void Count() {
        assertThat(employeeRepository.count()).isEqualTo(4);
    }

    @Test
    void Update() {
        employee_1.setSalary(15000L);
        employeeRepository.save(employee_1);

        Employee updated = employeeRepository.findById(employee_1.getId()).get();
        assertThat(updated.getSalary()).isEqualTo(15000L);
    }


    @Test
    void FindAllSortedByName() {
        List<Employee> sorted = employeeRepository.findAll(Sort.by("name"));
        assertThat(sorted).extracting(Employee::getName).containsExactly("Lebron", "Luka", "Manager", "Shai");

    }

    @Test
    void FindAllByExample() {
        Employee example = new Employee();
        example.setPosition("SF");

        List<Employee> result = employeeRepository.findAll(Example.of(example));
        assertThat(result).hasSize(1).extracting(Employee::getName).contains("Lebron");
    }

    @Test
    void DeleteAll() {
        employeeRepository.deleteAll();
        assertThat(employeeRepository.count()).isZero();
    }
}