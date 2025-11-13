package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void shouldSaveEmployeeAndAssignId() {
        Employee employee = new Employee();
        employee.setName("Muhammadqodir");
        employee.setPosition("Junior Dev");
        employee.setSalary(BigDecimal.valueOf(20000));
        Employee saved = employeeRepository.save(employee);
        assertNotNull(saved.getId());
        assertEquals("Muhammadqodir", saved.getName());
    }

    @Test
    void shouldFindEmployeeByIdAfterSave() {
        Employee employee = new Employee();
        employee.setName("Sarvar");
        employee.setPosition("DevOps");
        employee.setSalary(BigDecimal.valueOf(40000));
        Employee saved = entityManager.persistAndFlush(employee);

        Optional<Employee> found = employeeRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Sarvar", found.get().getName());
    }

    @Test
    void shouldReturnEmptyOptionalWhenIdNotExists() {
        Optional<Employee> found = employeeRepository.findById(999L);


        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindAllEmployees() {
        Employee emp1 = new Employee(null, "Ali", "Tester", BigDecimal.valueOf(30000));
        Employee emp2 = new Employee(null, "Vali", "Analyst", BigDecimal.valueOf(35000));
        entityManager.persist(emp1);
        entityManager.persist(emp2);
        entityManager.flush();

        List<Employee> all = employeeRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoEmployees() {
        List<Employee> all = employeeRepository.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    void shouldCountEmployeesCorrectly() {
        entityManager.persist(new Employee(null, "A", "B", BigDecimal.valueOf(1)));
        entityManager.persist(new Employee(null, "C", "D", BigDecimal.valueOf(2)));
        entityManager.flush();

        long count = employeeRepository.count();
        assertEquals(2, count);
    }

    @Test
    void shouldReturnTrueForExistsByIdWhenSaved() {
        Employee emp = new Employee(null, "X", "Y", BigDecimal.valueOf(100));
        Employee saved = entityManager.persistAndFlush(emp);

        boolean exists = employeeRepository.existsById(saved.getId());
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseForExistsByIdWhenNotSaved() {
        boolean exists = employeeRepository.existsById(999L);

        assertFalse(exists);
    }

    @Test
    void shouldDeleteEmployeeById() {
        Employee emp = new Employee(null, "Delete Me", "Temp", BigDecimal.valueOf(1));
        Employee saved = entityManager.persistAndFlush(emp);

        employeeRepository.deleteById(saved.getId());
        entityManager.flush();

        assertFalse(employeeRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldDeleteEmployeeEntity() {
        Employee emp = new Employee(null, "Delete Entity", "Temp", BigDecimal.valueOf(1));
        Employee saved = entityManager.persistAndFlush(emp);

        employeeRepository.delete(saved);
        entityManager.flush();

        assertEquals(0, employeeRepository.count());
    }

    @Test
    void shouldUpdateEmployeeFieldsAfterSave() {
        Employee emp = new Employee(null, "Old Name", "Old Pos", BigDecimal.valueOf(100));
        Employee saved = entityManager.persistAndFlush(emp);

        saved.setName("New Name");
        saved.setPosition("New Position");
        saved.setSalary(BigDecimal.valueOf(99999));
        employeeRepository.save(saved);

        Employee updated = entityManager.find(Employee.class, saved.getId());
        assertEquals("New Name", updated.getName());
        assertEquals("New Position", updated.getPosition());
    }

    @Test
    void shouldSaveMultipleEmployeesWithSaveAll() {

        Employee e1 = new Employee(null, "E1", "P1", BigDecimal.valueOf(1));
        Employee e2 = new Employee(null, "E2", "P2", BigDecimal.valueOf(2));
        List<Employee> employees = List.of(e1, e2);

        employeeRepository.saveAll(employees);
        entityManager.flush();

        assertEquals(2, employeeRepository.count());
    }

    @Test
    void shouldClearDatabaseAfterEachTest() {
        assertEquals(0, employeeRepository.count());
    }
}