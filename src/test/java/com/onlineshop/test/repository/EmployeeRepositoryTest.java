package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("Save employee")
    void saveEmployee_ShouldPersistEmployee() {
        Employee employee = new Employee();
        employee.setName("Abror");
        Employee saved = employeeRepository.save(employee);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Abror");
    }

    @Test
    @DisplayName("Find employee by id")
    void findById_ShouldReturnEmployee() {
        Employee employee = new Employee();
        employee.setName("Abror");
        Employee saved = employeeRepository.save(employee);

        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Abror");
    }

    @Test
    @DisplayName("Find employee by non-existent id")
    void findById_ShouldReturnEmpty() {
        Optional<Employee> found = employeeRepository.findById(1L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Find all employees")
    void findAll_ShouldReturnList() {
        employeeRepository.save(new Employee(null, "Abror", null, null, null, null));
        employeeRepository.save(new Employee(null, "Sardor", null, null, null, null));

        List<Employee> all = employeeRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Delete employee")
    void deleteEmployee_ShouldRemoveEmployee() {
        Employee employee = new Employee();
        employee.setName("Abror");
        Employee saved = employeeRepository.save(employee);

        employeeRepository.delete(saved);
        assertThat(employeeRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("Update employee")
    void updateEmployee_ShouldChangeFields() {
        Employee employee = new Employee();
        employee.setName("Abror");
        employee.setSalary(100L);
        Employee saved = employeeRepository.save(employee);

        saved.setSalary(200L);
        Employee updated = employeeRepository.save(saved);

        assertThat(updated.getSalary()).isEqualTo(200L);
        assertThat(updated.getName()).isEqualTo("Abror");
    }

    @Test
    @DisplayName("Find employees by name")
    void findByName_ShouldReturnMatchingEmployees() {
        Employee e1 = new Employee();
        e1.setName("Abror");
        employeeRepository.save(e1);

        Employee e2 = new Employee();
        e2.setName("Abror");
        employeeRepository.save(e2);

        List<Employee> found = employeeRepository.findAll().stream()
                .filter(emp -> "Abror".equals(emp.getName()))
                .toList();

        assertThat(found).hasSize(2);
    }

    @Test
    @DisplayName("Delete non-existent employee")
    void deleteNonExistentEmployee_ShouldNotFail() {
        Employee employee = new Employee();
        employee.setId(1L);
        employeeRepository.delete(employee);
        assertThat(employeeRepository.findById(1L)).isEmpty();
    }
}
