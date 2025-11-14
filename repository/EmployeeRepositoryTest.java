package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Employee;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    //    1 сохранение и получение сотрудника
    @Test
    @DisplayName("Сохраняем и получаем сотрудника в PostgreSQL через Docker")
    void saveAndFindEmployee() {
        Employee employee = new Employee();
        employee.setName("Lance");
        employee.setPosition("Manager");
        employee.setSalary(5000L);

        Employee saved = employeeRepository.save(employee);
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Lance");
    }

    //    2 удаление сотрудника
    @Test
    @DisplayName("удаление сотрдуника")
    void deleteEmployee() {
        Employee emp = new Employee();
        emp.setName("Lewis");
        emp.setPosition("Develooper");
        emp.setSalary(6900L);
        Employee saved = employeeRepository.save(emp);

        employeeRepository.deleteById(saved.getId());
        Optional<Employee> found = employeeRepository.findById(saved.getId());

        assertThat(found).isNotPresent();
    }

    //        3 список всех сотрудников
    @Test
    @DisplayName("все сотрудники")
    void findAllEMployees() {
        Employee e1 = new Employee();
        e1.setName("Max");
        Employee e2 = new Employee();
        e2.setName("Yuki");
        employeeRepository.saveAll(List.of(e1, e2));

        List<Employee> all = employeeRepository.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    //     4 обновление сотрудника
    @Test
    @DisplayName("Обновлуник сотрудниа")
    void updateEmployee() {
        Employee emp = new Employee();
        emp.setName("Lance");
        emp.setPosition("Manager");
        emp.setSalary(5000L);
        Employee saved = employeeRepository.save(emp);

        saved.setSalary(6000L);
        employeeRepository.save(saved);

        Optional<Employee> updated = employeeRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getSalary()).isEqualTo(6000L);
    }

    // 5 поиск того, кого нету
    @Test
    @DisplayName("Поиск несуществующего сотрудника")
    void findNonExistingEmployee() {
        Optional<Employee> emp = employeeRepository.findById(999L);
        assertThat(emp).isNotPresent();
    }

    // 6 fail - неверное имя после сохранения
    @Test
    @DisplayName("FAIL: имя сотрудника не совпадает")
    void failTestNameMismatch() {
        Employee emp = new Employee();
        emp.setName("Lance");
        Employee saved = employeeRepository.save(emp);

        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Max");
    }

    // 7 удаление сотрудника которого инопришленцы похитили
    @Test
    @DisplayName("FAIL: удаление несуществующего сотрудника")
    void failDeleteNonExisting() {
        assertThrows(Exception.class, () -> employeeRepository.deleteById(999L));
    }

    // 8 сохранение нескольких сотрудников и проверка count
    @Test
    @DisplayName("сохранение и проверка count")
    void saveMultipleAndCount() {
        Employee e1 = new Employee();
        e1.setName("A");
        Employee e2 = new Employee();
        e2.setName("B");
        employeeRepository.saveAll(List.of(e1, e2));

        long count = employeeRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    // 9 fail, неправильная зарплата после обновления
    @Test
    @DisplayName("FAIL: зарплата сотрудника неверная после обновления")
    void failUpdateSalary() {
        Employee emp = new Employee();
        emp.setName("Lance");
        emp.setSalary(5000L);
        Employee saved = employeeRepository.save(emp);

        saved.setSalary(6000L);
        employeeRepository.save(saved);

        Optional<Employee> updated = employeeRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getSalary()).isEqualTo(5000L);
    }

    // 10 findAll на пустой репозиторий
    @Test
    @DisplayName("findAll на пустой репозиторий")
    void findAllEmptyRepo() {
        employeeRepository.deleteAll();
        List<Employee> all = employeeRepository.findAll();
        assertThat(all).isEmpty();
    }
}
