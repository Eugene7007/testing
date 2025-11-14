package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers
class EmployeeRepositoryTest extends RepositoryBaseTest {

    private Employee employeeOne;
    private Employee employeeTwo;
    private Department department;

    @BeforeEach
    public void setUp() {
        department = new Department();
        department.setName("Finance");
        entityManager.persist(department);
        entityManager.flush();

        employeeOne = new Employee();
        employeeOne.setName("John Doe");
        employeeOne.setPosition("accountant");
        employeeOne.setSalary(3000L);
        employeeOne.setDepartment(department);

        employeeTwo = new Employee();
        employeeTwo.setName("Sam Serious");
        employeeTwo.setPosition("King of Kings");
        employeeTwo.setSalary(9999L);
        employeeTwo.setDepartment(department);
    }

    @Test
    void testSave() {
        Employee savedEmployee = employeeRepository.save(employeeOne);

        assertThat(savedEmployee.getId()).isNotNull();
        assertThat(savedEmployee.getDepartment()).isNotNull();
        assertThat(savedEmployee.getDepartment().getId()).isNotNull();

        Employee found = employeeRepository.findById(savedEmployee.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getDepartment()).isNotNull();
        assertThat(found.getDepartment().getId()).isNotNull();

        assertThat(found.getName()).isEqualTo("John Doe");
        assertThat(found.getPosition()).isEqualTo("accountant");
        assertThat(found.getSalary()).isEqualTo(3000L);
        assertThat(found.getDepartment().getName()).isEqualTo("Finance");
    }

    @Test
    void testFindAllEmployees() {
        employeeRepository.save(employeeOne);
        employeeRepository.save(employeeTwo);

        List<Employee> results = employeeRepository.findAll();

        assertThat(results).hasSize(2);
    }

    @Test
    void testDeleteEmployee() {
        Employee savedFirst = employeeRepository.save(employeeOne);
        Employee savedSecond = employeeRepository.save(employeeTwo);

        employeeRepository.deleteById(savedFirst.getId());

        assertThat(employeeRepository.findById(savedFirst.getId())).isNotPresent();
        assertThat(employeeRepository.findById(savedSecond.getId())).isPresent();
    }

    @Test
    void testUpdateEmployee() {
        Employee saved = employeeRepository.save(employeeOne);

        saved.setSalary(1800L);
        saved.setPosition("jun");
        saved.setName("just jun");

        employeeRepository.save(saved);

        Employee found = employeeRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getSalary()).isEqualTo(1800L);
        assertThat(found.getPosition()).isEqualTo("jun");
        assertThat(found.getName()).isEqualTo("just jun");
        assertThat(found.getDepartment().getName()).isEqualTo("Finance");
    }

    @Test
    void testFailSaveEmployeeWithoutDepartment() {
        employeeOne.setDepartment(null);

        assertThatThrownBy(() -> {
            employeeRepository.saveAndFlush(employeeOne);
            entityManager.flush();
        })
                .isInstanceOf(Exception.class); // could refine if needed
    }

    @Test
    void testFailSaveEmployeeWithNegativeSalary() {
        employeeOne.setSalary(-999L);

        assertThatThrownBy(() -> {
            employeeRepository.saveAndFlush(employeeOne);
            entityManager.flush();
        })
                .isInstanceOf(Exception.class);
    }

    @Test
    void testFindNonExistingEmployee() {
        assertThat(employeeRepository.findById(-999L)).isNotPresent();
    }

    @Test
    void testNonExistingEmployeeDoesNotExistsById() {
        assertThat(employeeRepository.existsById(-999L)).isFalse();
    }

    @Test
    void testDeleteNonExistingEmployee() {
        assertThat(employeeRepository.existsById(-999L)).isFalse();

        employeeRepository.deleteById(-999L);

        assertThat(employeeRepository.existsById(-999L)).isFalse();
    }


    @Test
    void testFailSaveEmployeeWithNullName() {
        employeeOne.setName(null);

        assertThatThrownBy(() -> {
            employeeRepository.saveAndFlush(employeeOne);
            entityManager.flush();
        })
                .isInstanceOf(Exception.class);
    }
}