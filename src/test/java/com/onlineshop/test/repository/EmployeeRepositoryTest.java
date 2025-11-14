package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager em;

    private Employee newEmployee(String name) {
        var e = new Employee();
        e.setName(name);
        e.setPosition("Developer");
        e.setSalary(100000L);
        return e;
    }

    @Test
    @DisplayName("save генерирует id и сохраняет поля")
    void save_ShouldGenerateId_AndPersistFields() {
        var saved = employeeRepository.save(newEmployee("Alice"));
        assertThat(saved.getId()).isNotNull();

        var found = employeeRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Alice");
        assertThat(found.getPosition()).isEqualTo("Developer");
        assertThat(found.getSalary()).isEqualTo(100000L);
    }

    @Test
    @DisplayName("findById возвращает сохранённую запись")
    void findById_ShouldReturnSavedEmployee() {
        var saved = employeeRepository.save(newEmployee("Bob"));
        var found = employeeRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("findAll содержит ранее сохранённые записи")
    void findAll_ShouldContainSavedEmployees() {
        var e1 = employeeRepository.save(newEmployee("E1"));
        var e2 = employeeRepository.save(newEmployee("E2"));

        List<Employee> all = employeeRepository.findAll();
        assertThat(all)
            .extracting(Employee::getId)
            .contains(e1.getId(), e2.getId());
    }

    @Test
    @DisplayName("update обновляет поля и сохраняет изменения")
    void update_ShouldPersistChanges() {
        var e = employeeRepository.save(newEmployee("ToUpdate"));
        e.setName("Updated");
        e.setSalary(150000L);

        employeeRepository.save(e);

        var found = employeeRepository.findById(e.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Updated");
        assertThat(found.getSalary()).isEqualTo(150000L);
    }

    @Test
    @DisplayName("deleteById удаляет запись")
    void deleteById_ShouldRemoveEmployee() {
        var e = employeeRepository.save(newEmployee("ToDelete"));
        Long id = e.getId();

        employeeRepository.deleteById(id);

        assertThat(employeeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("ManyToOne manager (self-reference): подчинённый ссылается на менеджера")
    void save_WithManagerRelation_ShouldPersistSelfReference() {
        var manager = employeeRepository.save(newEmployee("Manager"));
        var subordinate = newEmployee("Subordinate");
        subordinate.setManager(manager);

        var savedSub = employeeRepository.save(subordinate);

        var foundSub = employeeRepository.findById(savedSub.getId()).orElseThrow();
        assertThat(foundSub.getManager()).isNotNull();
        assertThat(foundSub.getManager().getId()).isEqualTo(manager.getId());
        assertThat(foundSub.getManager().getName()).isEqualTo("Manager");
    }

    @Test
    @DisplayName("ManyToOne department: сотрудник связан с департаментом")
    void save_WithDepartmentRelation_ShouldPersistDepartment() {
        var dep = new Department();
        dep.setName("IT");
        dep.setLocation("HQ");
        dep = em.persistAndFlush(dep);

        var emp = newEmployee("Carol");
        emp.setDepartment(dep);

        var saved = employeeRepository.save(emp);

        var found = employeeRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getDepartment()).isNotNull();
        assertThat(found.getDepartment().getId()).isEqualTo(dep.getId());
        assertThat(found.getDepartment().getName()).isEqualTo("IT");
    }

    @Test
    @DisplayName("Пагинация и сортировка по зарплате (DESC)")
    void findAll_PaginationAndSortingBySalaryDesc() {
        var low = newEmployee("Low");   low.setSalary(90000L);
        var mid = newEmployee("Mid");   mid.setSalary(110000L);
        var high = newEmployee("High"); high.setSalary(150000L);
        employeeRepository.saveAll(List.of(low, mid, high));

        var page0 = employeeRepository.findAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "salary")));
        assertThat(page0.getNumberOfElements()).isEqualTo(2);
        assertThat(page0.getContent())
            .extracting(Employee::getName)
            .containsExactly("High", "Mid");

        var page1 = employeeRepository.findAll(PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "salary")));
        assertThat(page1.getNumberOfElements()).isEqualTo(1);
        assertThat(page1.getContent())
            .extracting(Employee::getName)
            .containsExactly("Low");
    }

    @Test
    @DisplayName("Перевод сотрудника в другой департамент обновляет FK department_id")
    void reassignDepartment_ShouldUpdateForeignKey() {
        var it = new Department(); it.setName("IT"); it.setLocation("HQ");
        var hr = new Department(); hr.setName("HR"); hr.setLocation("HQ");
        it = em.persistAndFlush(it);
        hr = em.persistAndFlush(hr);

        var emp = newEmployee("Carol");
        emp.setDepartment(it);
        emp = employeeRepository.save(emp);
        em.flush();

        emp.setDepartment(hr);
        employeeRepository.save(emp);
        em.flush();

        var found = employeeRepository.findById(emp.getId()).orElseThrow();
        assertThat(found.getDepartment()).isNotNull();
        assertThat(found.getDepartment().getId()).isEqualTo(hr.getId());
        assertThat(found.getDepartment().getName()).isEqualTo("HR");
    }
}