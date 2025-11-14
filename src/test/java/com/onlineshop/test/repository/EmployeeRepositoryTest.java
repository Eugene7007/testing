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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@DisplayName("Employee Repository Integration Tests")
class EmployeeRepositoryIntegrationTest {
// Тут могут быть проблемы с созданием контейнера, даже у меня были, не знаю как исправить.
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

    private Department testDepartment;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setName("IT Department");
        testDepartment = departmentRepository.save(testDepartment);

        testEmployee = new Employee();
        testEmployee.setName("John Doe");
        testEmployee.setPosition("Developer");
        testEmployee.setSalary(50000L);
        testEmployee.setDepartment(testDepartment);
    }

    @Test
    @DisplayName("save() - Should save employee and assign ID")
    void save_shouldSaveEmployee_andAssignId() {
        Employee saved = employeeRepository.save(testEmployee);

        assertThat(saved.getId())
            .isNotNull()
            .isPositive();

        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getPosition()).isEqualTo("Developer");
        assertThat(saved.getSalary()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("save() - Should save employee with manager relationship")
    void save_shouldSaveEmployee_withManager() {
        Employee manager = new Employee();
        manager.setName("Manager Bob");
        manager.setPosition("Team Lead");
        manager.setSalary(70000L);
        manager.setDepartment(testDepartment);
        manager = employeeRepository.save(manager);

        testEmployee.setManager(manager);

        Employee saved = employeeRepository.save(testEmployee);

        assertThat(saved.getManager()).isNotNull();
        assertThat(saved.getManager().getId()).isEqualTo(manager.getId());
        assertThat(saved.getManager().getName()).isEqualTo("Manager Bob");
    }

    @Test
    @DisplayName("save() - Should update existing employee")
    void save_shouldUpdateExistingEmployee() {
        Employee saved = employeeRepository.save(testEmployee);
        Long employeeId = saved.getId();

        saved.setName("John Updated");
        saved.setSalary(60000L);
        Employee updated = employeeRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(employeeId);
        assertThat(updated.getName()).isEqualTo("John Updated");
        assertThat(updated.getSalary()).isEqualTo(60000L);
    }

    @Test
    @DisplayName("findById() - Should return employee when ID exists")
    void findById_shouldReturnEmployee_whenIdExists() {
        Employee saved = employeeRepository.save(testEmployee);

        Optional<Employee> found = employeeRepository.findById(saved.getId());

        assertThat(found)
            .isPresent()
            .get()
            .satisfies(employee -> { // Проверяем все поля
                assertThat(employee.getId()).isEqualTo(saved.getId());
                assertThat(employee.getName()).isEqualTo("John Doe");
                assertThat(employee.getPosition()).isEqualTo("Developer");
                assertThat(employee.getSalary()).isEqualTo(50000L);
            });
    }

    @Test
    @DisplayName("findById() - Should return empty Optional when ID does not exist")
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {
        Optional<Employee> found = employeeRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findById() - Should load department relationship")
    void findById_shouldLoadDepartment() {
        Employee saved = employeeRepository.save(testEmployee);

        Employee found = employeeRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getDepartment()).isNotNull();
        assertThat(found.getDepartment().getName()).isEqualTo("IT Department");
    }

    @Test
    @DisplayName("findAll() - Should return all employees")
    void findAll_shouldReturnAllEmployees() {
        employeeRepository.save(testEmployee);

        Employee employee2 = new Employee();
        employee2.setName("Jane Smith");
        employee2.setPosition("Designer");
        employee2.setSalary(45000L);
        employee2.setDepartment(testDepartment);
        employeeRepository.save(employee2);

        List<Employee> employees = employeeRepository.findAll();

        assertThat(employees)
            .hasSize(2)
            .extracting(Employee::getName)
            .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    @DisplayName("findAll() - Should return empty list when no employees exist")
    void findAll_shouldReturnEmptyList_whenNoEmployees() {
        List<Employee> employees = employeeRepository.findAll();

        assertThat(employees).isEmpty();
    }

    @Test
    @DisplayName("findAll() - Should return employees in order of creation")
    void findAll_shouldReturnEmployeesInOrder() {
        Department dep1 = new Department();
        dep1.setId(1L);
        Employee manager1 = new Employee();
        manager1.setId(1L);
        Department dep2 = new Department();
        dep2.setId(2L);
        Employee manager2 = new Employee();
        manager2.setId(2L);
        Department dep3 = new Department();
        dep3.setId(3L);
        Employee manager3 = new Employee();
        manager3.setId(3L);

        Employee emp1 = new Employee(50000L, "Alice", "Developer", 1000L, dep1, manager1);
        Employee emp2 = new Employee(60000L, "Bob", "Manager", 20000L,  dep2, manager2);
        Employee emp3 = new Employee(45000L, "Charlie", "Designer", 4000L, dep3, manager3);

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);

        List<Employee> employees = employeeRepository.findAll();

        assertThat(employees)
            .hasSize(3)
            .extracting(Employee::getName)
            .containsExactly("Alice", "Bob", "Charlie");
    }

    @Test
    @DisplayName("deleteById() - Should delete employee when ID exists")
    void deleteById_shouldDeleteEmployee_whenIdExists() {
        Employee saved = employeeRepository.save(testEmployee);
        Long employeeId = saved.getId();

        employeeRepository.deleteById(employeeId);

        Optional<Employee> deleted = employeeRepository.findById(employeeId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("delete() - Should delete employee entity")
    void delete_shouldDeleteEmployee() {
        Employee saved = employeeRepository.save(testEmployee);
        Long employeeId = saved.getId();

        employeeRepository.delete(saved);

        assertThat(employeeRepository.findById(employeeId)).isEmpty();
    }

    @Test
    @DisplayName("deleteAll() - Should delete all employees")
    void deleteAll_shouldDeleteAllEmployees() {
        Department dep1 = new Department();
        dep1.setId(1L);
        Employee manager1 = new Employee();
        manager1.setId(1L);
        Department dep2 = new Department();
        dep2.setId(2L);
        Employee manager2 = new Employee();
        manager2.setId(2L);

        employeeRepository.save(testEmployee);
        employeeRepository.save(new Employee(50000L, "Alice", "Developer", 1000L, dep1, manager1));
        employeeRepository.save(new Employee(60000L, "Bob", "Manager", 20000L,  dep2, manager2));

        assertThat(employeeRepository.findAll()).hasSize(3);

        employeeRepository.deleteAll();

        assertThat(employeeRepository.findAll()).isEmpty();
    }


    @Test
    @DisplayName("existsById() - Should return true when employee exists")
    void existsById_shouldReturnTrue_whenEmployeeExists() {
        Employee saved = employeeRepository.save(testEmployee);

        boolean exists = employeeRepository.existsById(saved.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById() - Should return false when employee does not exist")
    void existsById_shouldReturnFalse_whenEmployeeDoesNotExist() {
        boolean exists = employeeRepository.existsById(9999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("count() - Should return correct count of employees")
    void count_shouldReturnCorrectCount() {
        Department dep2 = new Department();
        dep2.setId(2L);
        Employee manager2 = new Employee();
        manager2.setId(2L);
        Department dep3 = new Department();
        dep3.setId(3L);
        Employee manager3 = new Employee();
        manager3.setId(3L);

        employeeRepository.save(testEmployee);
        employeeRepository.save(new Employee(50000L, "Alice", "Developer", 1000L, dep2, manager2));
        employeeRepository.save(new Employee(60000L, "Bob", "Manager", 20000L,  dep3, manager3));

        long count = employeeRepository.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("count() - Should return zero when no employees exist")
    void count_shouldReturnZero_whenNoEmployees() {
        long count = employeeRepository.count();

        assertThat(count).isZero();
    }
}