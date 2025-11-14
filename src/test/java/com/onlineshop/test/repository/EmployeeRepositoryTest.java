package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee savedEmployee;

    @BeforeEach
    void setup() {
        employeeRepository.deleteAll();
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setSalary(5000L);
        savedEmployee = employeeRepository.save(employee);
    }

    //test1
    @Test
    void testSaveEmployee() {
        Employee employee = new Employee();
        employee.setName("Alice");
        employee.setSalary(6000L);

        Employee saved = employeeRepository.save(employee);
        assertThat(saved.getId()).isNotNull();
    }

    //test2
    @Test
    void testFindById() {
        Optional<Employee> employee = employeeRepository.findById(savedEmployee.getId());
        assertThat(employee).isPresent();
        assertThat(employee.get().getName()).isEqualTo("John Doe");
    }

    //test3
    @Test
    void testFindById_NotFound() {
        Optional<Employee> employee = employeeRepository.findById(999L);
        assertThat(employee).isNotPresent();
    }

    //test4
    @Test
    void testFindAll() {
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).hasSize(1);
    }

    //test5
    @Test
    void testDeleteEmployee() {
        employeeRepository.delete(savedEmployee);
        assertThat(employeeRepository.findById(savedEmployee.getId())).isNotPresent();
    }

    //tes6
    @Test
    void testDeleteById() {
        employeeRepository.deleteById(savedEmployee.getId());
        assertThat(employeeRepository.findById(savedEmployee.getId())).isNotPresent();
    }

    //test7
    @Test
    void testUpdateEmployee() {
        savedEmployee.setName("Updated Name");
        employeeRepository.save(savedEmployee);

        Employee updated = employeeRepository.findById(savedEmployee.getId()).get();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    //test8
    @Test
    void testExistsById() {
        boolean exists = employeeRepository.existsById(savedEmployee.getId());
        assertThat(exists).isTrue();
    }

    //test9
    @Test
    void testCount() {
        long count = employeeRepository.count();
        assertThat(count).isEqualTo(1);
    }

    //test10
    @Test
    void testSaveMultipleEmployees() {
        Employee emp1 = new Employee();
        emp1.setName("Emp1");
        emp1.setSalary(1000L);

        Employee emp2 = new Employee();
        emp2.setName("Emp2");
        emp2.setSalary(2000L);

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);

        assertThat(employeeRepository.findAll()).hasSize(3);
    }

    //test11
    @Test
    void testFindByName() {
        List<Employee> employees = employeeRepository.findAll()
                .stream()
                .filter(e -> e.getName().equals("John Doe"))
                .toList();
        assertThat(employees).hasSize(1);
    }
}
