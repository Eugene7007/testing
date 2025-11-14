package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;


    private Employee createSampleEmployee() {
        Employee emp = new Employee();
        emp.setName("Dilmurod");
        emp.setPosition("Developer");
        emp.setSalary(BigDecimal.valueOf(1000));
        return emp;
    }


    @Test
    void testSaveEmployee() {
        Employee saved = employeeRepository.save(createSampleEmployee());
        assertThat(saved.getId()).isNotNull();
    }


    @Test
    void testFindAllEmpty() {
        List<Employee> list = employeeRepository.findAll();
        assertThat(list).isEmpty();
    }


    @Test
    void testFindAllAfterSave() {
        employeeRepository.save(createSampleEmployee());
        assertThat(employeeRepository.findAll()).hasSize(1);
    }


    @Test
    void testFindByIdExists() {
        Employee emp = employeeRepository.save(createSampleEmployee());
        Optional<Employee> found = employeeRepository.findById(emp.getId());
        assertThat(found).isPresent();
    }


    @Test
    void testFindByIdNotExists() {
        Optional<Employee> found = employeeRepository.findById(99L);
        assertThat(found).isNotPresent();
    }


    @Test
    void testUpdateEmployee() {
        Employee emp = employeeRepository.save(createSampleEmployee());
        emp.setName("Updated");
        Employee updated = employeeRepository.save(emp);
        assertThat(updated.getName()).isEqualTo("Updated");
    }

    @Test
    void testDeleteEmployee() {
        Employee emp = employeeRepository.save(createSampleEmployee());
        employeeRepository.deleteById(emp.getId());
        assertThat(employeeRepository.findById(emp.getId())).isNotPresent();
    }


    @Test
    void testSaveMultipleEmployees() {
        employeeRepository.save(createSampleEmployee());
        employeeRepository.save(createSampleEmployee());
        assertThat(employeeRepository.findAll()).hasSize(2);
    }


    @Test
    void testDeleteAllEmployees() {
        employeeRepository.save(createSampleEmployee());
        employeeRepository.save(createSampleEmployee());
        employeeRepository.deleteAll();
        assertThat(employeeRepository.findAll()).isEmpty();
    }


    @Test
    void testCountEmployees() {
        employeeRepository.save(createSampleEmployee());
        employeeRepository.save(createSampleEmployee());
        assertThat(employeeRepository.count()).isEqualTo(2);
    }


    @Test
    void testSaveEmployeeWithSalary() {
        Employee emp = createSampleEmployee();
        emp.setSalary(BigDecimal.valueOf(2500));

        Employee saved = employeeRepository.save(emp);
        assertThat(saved.getSalary()).isEqualTo(BigDecimal.valueOf(2500));
    }


    @Test
    void testUpdatePosition() {
        Employee emp = employeeRepository.save(createSampleEmployee());
        emp.setPosition("Manager");
        Employee saved = employeeRepository.save(emp);

        assertThat(saved.getPosition()).isEqualTo("Manager");
    }
}
