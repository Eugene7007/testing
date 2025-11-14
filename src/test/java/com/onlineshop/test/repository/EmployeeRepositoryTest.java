package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Department;
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
import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
public class EmployeeRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department itDept;
    private Department qaDept;

    private Employee manager;

    @BeforeEach
    void setup() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        itDept = new Department();
        itDept.setName("IT");
        departmentRepository.save(itDept);

        qaDept = new Department();
        qaDept.setName("QA");
        departmentRepository.save(qaDept);

        manager = new Employee(null, "Manager Anne", "Manager", 8000L, itDept, null);
        employeeRepository.save(manager);
    }
//Проверяем, что нового сотрудника можно сохранить в базе
    @Test
    void saveEmployee() {
        Employee emp = new Employee(null, "Alex", "Developer", 5000L, itDept, null);
        Employee saved = employeeRepository.save(emp);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Alex");
    }
//Ищем сотрудника по несуществующему id
    @Test
    void findById_notExists() {
        Optional<Employee> found = employeeRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    //Сохраняем несколько сотрудников и проверяем, что возвращает всех
    @Test
    void findAll() {
        employeeRepository.save(new Employee(null, "Anna", "Dev", 4000L, itDept, null));
        employeeRepository.save(new Employee(null, "Emma", "QA", 3500L, qaDept, null));
        List<Employee> all = employeeRepository.findAll();
        assertThat(all.size()).isEqualTo(3);
    }
    //Меняем зарплату employee и сохраняем
    @Test
    void updateEmployee_salary() {
        Employee emp = new Employee(null, "Kate", "Developer", 4500L, itDept, null);
        employeeRepository.save(emp);

        emp.setSalary(5000L);
        Employee updated = employeeRepository.save(emp);

        assertEquals(5000L, updated.getSalary());
    }

    @Test
    void deleteEmployee() {
        Employee emp = new Employee(null, "Max", "Tester", 3000L, qaDept, null);
        employeeRepository.save(emp);

        employeeRepository.delete(emp);
        assertFalse(employeeRepository.findById(emp.getId()).isPresent());
    }

    @Test
    void deleteAll() {
        employeeRepository.deleteAll();
        assertThat(employeeRepository.findAll()).isEmpty();
    }
//Меняем должность менеджера
    @Test
    void updateEmployeePosition() {
        manager.setPosition("Senior Manager");
        employeeRepository.save(manager);

        Employee updated = employeeRepository.findById(manager.getId()).orElseThrow();
        assertEquals("Senior Manager", updated.getPosition());
    }

    @Test
    void findEmployeesByManager() {
        Employee emp1 = new Employee(null, "A", "Dev", 4000L, itDept, manager);
        Employee emp2 = new Employee(null, "B", "Dev", 4500L, itDept, null);
        employeeRepository.save(emp1);
        employeeRepository.save(emp2);

        List<Employee> managedByManager = employeeRepository.findAll()
                .stream()
                .filter(e -> manager.equals(e.getManager()))
                .toList();

        assertThat(managedByManager).contains(emp1).doesNotContain(emp2);
    }

    //Проверяем, что у сотрудника корректно сохраняется связь с менеджером
    @Test
    void employeeManagerRelationship() {
        Employee emp = new Employee(null, "Abc", "Dev", 4000L, itDept, manager);
        employeeRepository.save(emp);

        Employee saved = employeeRepository.findById(emp.getId()).orElseThrow();
        assertThat(saved.getManager()).isNotNull();
        assertEquals(manager.getId(), saved.getManager().getId());
    }

    @Test
    void cantSaveWithNegativeSalary() {
        Employee emp = new Employee(null, "Aaa", "Dev", -1000L, itDept, null);
        assertThrows(Exception.class, () -> employeeRepository.saveAndFlush(emp));
    }

//employees у которых нет менеджера.
    @Test
    void findEmployeesWithoutManager() {
        Employee emp1 = new Employee(null, "Bb", "Dev", 4000L, itDept, null);
        employeeRepository.save(emp1);

        List<Employee> noManager = employeeRepository.findAll()
                .stream()
                .filter(e -> e.getManager() == null)
                .toList();

        assertThat(noManager).contains(manager, emp1);
    }

}
