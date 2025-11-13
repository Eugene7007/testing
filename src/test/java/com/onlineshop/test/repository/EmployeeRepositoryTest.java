package com.onlineshop.test.repository;


import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Autowired
    private TestEntityManager entityManager;

    private Employee employee;

    @BeforeEach
    public void setUp() {
        employee = new Employee();
        employee.setName("Jon Snow");
        employee.setPosition("Lord Commander");
    }


    // test 1
    @Test
    void testSave() {
        Employee savedEmployee = employeeRepository.save(employee);
        assertNotNull(savedEmployee.getId());
    }


    // test 2
    @Test
    void testFindById() {
        Employee saved = employeeRepository.save(employee);
        entityManager.flush();
        entityManager.clear();

        Optional<Employee> found = employeeRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Jon Snow", found.get().getName());
    }

    // 3
    @Test
    @Transactional
    void testFindAll() {
        Department department = new Department(null, "Differential diagnosis", "Princeton");
        Department department2 = new Department(null, "Oncology", "Princeton");

        departmentRepository.saveAll(List.of(department, department2));

        entityManager.flush();

        Employee emp1 = new Employee(null, "Gregory House", "Doctor", 50000L, department, null);
        Employee emp2 = new Employee(null, "James Wilson", "Doctor", 60000L, department2, null);

        employeeRepository.saveAll(List.of(employee, emp1, emp2));

        entityManager.flush();

        List<Employee> all = employeeRepository.findAll();

        assertEquals(3, all.size());
    }

    // 4
    @Test
    void testUpdate() {
        Employee saved = employeeRepository.save(employee);
        entityManager.flush();

        String newPosition = "King in the North";

        saved.setPosition(newPosition);
        Employee updated = employeeRepository.save(saved);

        assertEquals(newPosition, entityManager.find(Employee.class, updated.getId()).getPosition());
    }

    // 5
    @Test
    void testDeleteById() {
        Employee saved = employeeRepository.save(employee);
        entityManager.flush();

        employeeRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        assertFalse(employeeRepository.findById(saved.getId()).isPresent());
    }

    // 6
    @Test
    void testCount() {
        employeeRepository.save(employee);
        entityManager.flush();

        long count = employeeRepository.count();
        assertEquals(1, count);
    }

    // 7
    @Test
    void testExistsById() {
        Employee saved = employeeRepository.save(employee);
        entityManager.flush();

        boolean exists = employeeRepository.existsById(saved.getId());
        assertTrue(exists);

        boolean notExists = employeeRepository.existsById(999L);
        assertFalse(notExists);
    }

    // 8
    @Test
    void testFindAll_WithSorting() {
        Department dept = departmentRepository.save(new Department(null, "IT", "NY"));
        employeeRepository.saveAll(List.of(
            new Employee(null, "Zoe", "Engineer", 70000L, dept, null),
            new Employee(null, "Alice", "Manager", 90000L, dept, null),
            new Employee(null, "Bob", "Analyst", 60000L, dept, null)
        ));
        entityManager.flush();
        entityManager.clear();

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<Employee> sorted = employeeRepository.findAll(sort);

        assertEquals(3, sorted.size());
        assertEquals("Alice", sorted.get(0).getName());
        assertEquals("Bob", sorted.get(1).getName());
        assertEquals("Zoe", sorted.get(2).getName());
    }

    // 9
    @Test
    void testDeleteAll() {
        departmentRepository.save(new Department(null, "TheWall", "North"));
        employeeRepository.saveAll(List.of(
            new Employee(null, "Donal Noe", "Smith", 100L, null, null),
            employee
        ));

        entityManager.flush();

        employeeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, employeeRepository.count());
    }

    // 10
    @Test
    void testFindByWrongId() {
        Employee found = employeeRepository.findById(9999L).orElse(null);
        assertNull(found);
    }

    // 11
    @Test
    void testFindAll_WithPagination() {
        Department dept = departmentRepository.save(new Department(null, "HR", "LA"));
        employeeRepository.saveAll(List.of(
            employee,
            new Employee(null, "Emp2", "Role", 200L, dept, null),
            new Employee(null, "Emp3", "Role", 300L, dept, null)
        ));
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 2);
        Page<Employee> page = employeeRepository.findAll(pageable);

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
        assertTrue(page.hasNext());

        Page<Employee> secondPage = employeeRepository.findAll(page.nextPageable());
        assertEquals(1, secondPage.getContent().size());
    }
}
