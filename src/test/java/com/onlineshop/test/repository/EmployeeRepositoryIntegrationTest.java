package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Integration tests for EmployeeRepository")
public class EmployeeRepositoryIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

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

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setName("IT Department");
        testDepartment.setLocation("Moscow");
        testDepartment = departmentRepository.save(testDepartment);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    // Тест 1: Сохранение сотрудника
    @Test
    @DisplayName("save() - should save employee successfully")
    void save_ShouldSaveEmployee_WhenValidEmployee() {
        // Arrange
        var employee = new Employee();
        employee.setName("Иван Иванов");
        employee.setPosition("Software Developer");
        employee.setSalary(100000L);
        employee.setDepartment(testDepartment);

        // Act
        var savedEmployee = employeeRepository.save(employee);

        // Assert
        assertThat(savedEmployee).isNotNull();
        assertThat(savedEmployee.getId()).isNotNull();
        assertThat(savedEmployee.getName()).isEqualTo("Иван Иванов");
        assertThat(savedEmployee.getPosition()).isEqualTo("Software Developer");
        assertThat(savedEmployee.getSalary()).isEqualTo(100000L);
        assertThat(savedEmployee.getDepartment()).isEqualTo(testDepartment);
    }

    // Тест 2: Поиск сотрудника по ID
    @Test
    @DisplayName("findById() - should return employee when exists")
    void findById_ShouldReturnEmployee_WhenEmployeeExists() {
        // Arrange
        var employee = new Employee();
        employee.setName("Петр Петров");
        employee.setPosition("QA Engineer");
        employee.setSalary(80000L);
        employee.setDepartment(testDepartment);
        var savedEmployee = employeeRepository.save(employee);

        // Act
        var foundEmployee = employeeRepository.findById(savedEmployee.getId());

        // Assert
        assertThat(foundEmployee).isPresent();
        assertThat(foundEmployee.get().getId()).isEqualTo(savedEmployee.getId());
        assertThat(foundEmployee.get().getName()).isEqualTo("Петр Петров");
        assertThat(foundEmployee.get().getPosition()).isEqualTo("QA Engineer");
    }

    // Тест 3: Поиск несуществующего сотрудника
    @Test
    @DisplayName("findById() - should return empty when employee does not exist")
    void findById_ShouldReturnEmpty_WhenEmployeeDoesNotExist() {
        // Act
        var foundEmployee = employeeRepository.findById(99999L);

        // Assert
        assertThat(foundEmployee).isEmpty();
    }

    // Тест 4: Получение всех сотрудников
    @Test
    @DisplayName("findAll() - should return all employees")
    void findAll_ShouldReturnAllEmployees_WhenEmployeesExist() {
        // Arrange
        var employee1 = new Employee();
        employee1.setName("Анна Смирнова");
        employee1.setPosition("Designer");
        employee1.setSalary(75000L);
        employee1.setDepartment(testDepartment);

        var employee2 = new Employee();
        employee2.setName("Сергей Кузнецов");
        employee2.setPosition("DevOps");
        employee2.setSalary(95000L);
        employee2.setDepartment(testDepartment);

        var employee3 = new Employee();
        employee3.setName("Мария Волкова");
        employee3.setPosition("Analyst");
        employee3.setSalary(70000L);
        employee3.setDepartment(testDepartment);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);

        // Act
        var allEmployees = employeeRepository.findAll();

        // Assert
        assertThat(allEmployees)
                .hasSize(3)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Анна Смирнова", "Сергей Кузнецов", "Мария Волкова");
    }

    // Тест 5: Получение всех сотрудников - пустой список
    @Test
    @DisplayName("findAll() - should return empty list when no employees exist")
    void findAll_ShouldReturnEmptyList_WhenNoEmployeesExist() {
        // Act
        var allEmployees = employeeRepository.findAll();

        // Assert
        assertThat(allEmployees).isEmpty();
    }

    // Тест 6: Удаление сотрудника
    @Test
    @DisplayName("delete() - should delete employee successfully")
    void delete_ShouldDeleteEmployee_WhenEmployeeExists() {
        // Arrange
        var employee = new Employee();
        employee.setName("Дмитрий Сидоров");
        employee.setPosition("Manager");
        employee.setSalary(90000L);
        employee.setDepartment(testDepartment);
        var savedEmployee = employeeRepository.save(employee);

        // Act
        employeeRepository.delete(savedEmployee);

        // Assert
        var foundEmployee = employeeRepository.findById(savedEmployee.getId());
        assertThat(foundEmployee).isEmpty();
        assertThat(employeeRepository.existsById(savedEmployee.getId())).isFalse();
    }

    // Тест 7: Удаление по ID
    @Test
    @DisplayName("deleteById() - should delete employee by id successfully")
    void deleteById_ShouldDeleteEmployee_WhenEmployeeExists() {
        // Arrange
        var employee = new Employee();
        employee.setName("Елена Павлова");
        employee.setPosition("HR Manager");
        employee.setSalary(85000L);
        employee.setDepartment(testDepartment);
        var savedEmployee = employeeRepository.save(employee);
        Long employeeId = savedEmployee.getId();

        // Act
        employeeRepository.deleteById(employeeId);

        // Assert
        assertThat(employeeRepository.findById(employeeId)).isEmpty();
        assertThat(employeeRepository.count()).isZero();
    }

    // Тест 8: Подсчет сотрудников
    @Test
    @DisplayName("count() - should return correct count of employees")
    void count_ShouldReturnCorrectCount_WhenEmployeesExist() {
        // Arrange
        var employee1 = new Employee();
        employee1.setName("Андрей Козлов");
        employee1.setPosition("Developer");
        employee1.setSalary(95000L);
        employee1.setDepartment(testDepartment);

        var employee2 = new Employee();
        employee2.setName("Ольга Новикова");
        employee2.setPosition("Tester");
        employee2.setSalary(72000L);
        employee2.setDepartment(testDepartment);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        // Act
        var count = employeeRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    // Тест 9: Проверка существования сотрудника
    @Test
    @DisplayName("existsById() - should return true when employee exists")
    void existsById_ShouldReturnTrue_WhenEmployeeExists() {
        // Arrange
        var employee = new Employee();
        employee.setName("Николай Морозов");
        employee.setPosition("Team Lead");
        employee.setSalary(150000L);
        employee.setDepartment(testDepartment);
        var savedEmployee = employeeRepository.save(employee);

        // Act
        var exists = employeeRepository.existsById(savedEmployee.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    // Тест 10: Проверка несуществующего сотрудника
    @Test
    @DisplayName("existsById() - should return false when employee does not exist")
    void existsById_ShouldReturnFalse_WhenEmployeeDoesNotExist() {
        // Act
        var exists = employeeRepository.existsById(99999L);

        // Assert
        assertThat(exists).isFalse();
    }

    // Тест 11: Обновление сотрудника
    @Test
    @DisplayName("save() - should update employee when already exists")
    void save_ShouldUpdateEmployee_WhenEmployeeAlreadyExists() {
        // Arrange
        var employee = new Employee();
        employee.setName("Виктор Соколов");
        employee.setPosition("Junior Developer");
        employee.setSalary(60000L);
        employee.setDepartment(testDepartment);
        var savedEmployee = employeeRepository.save(employee);

        // Act - обновляем данные
        savedEmployee.setPosition("Middle Developer");
        savedEmployee.setSalary(85000L);
        var updatedEmployee = employeeRepository.save(savedEmployee);

        // Assert
        assertThat(updatedEmployee.getId()).isEqualTo(savedEmployee.getId());
        assertThat(updatedEmployee.getPosition()).isEqualTo("Middle Developer");
        assertThat(updatedEmployee.getSalary()).isEqualTo(85000L);

        // Проверяем что в БД один сотрудник
        assertThat(employeeRepository.count()).isEqualTo(1);
    }

    // Тест 12: Удаление всех сотрудников
    @Test
    @DisplayName("deleteAll() - should delete all employees")
    void deleteAll_ShouldDeleteAllEmployees_WhenEmployeesExist() {
        // Arrange
        var employee1 = new Employee();
        employee1.setName("Алексей Новиков");
        employee1.setPosition("Backend Developer");
        employee1.setSalary(110000L);
        employee1.setDepartment(testDepartment);

        var employee2 = new Employee();
        employee2.setName("Татьяна Белова");
        employee2.setPosition("Frontend Developer");
        employee2.setSalary(105000L);
        employee2.setDepartment(testDepartment);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        // Act
        employeeRepository.deleteAll();

        // Assert
        assertThat(employeeRepository.findAll()).isEmpty();
        assertThat(employeeRepository.count()).isZero();
    }

    // Тест 13: Связь сотрудника с департаментом
    @Test
    @DisplayName("save() - should save employee with department relationship")
    void save_ShouldSaveEmployeeWithDepartment_WhenDepartmentProvided() {
        // Arrange
        var employee = new Employee();
        employee.setName("Константин Лебедев");
        employee.setPosition("Data Analyst");
        employee.setSalary(88000L);
        employee.setDepartment(testDepartment);

        // Act
        var savedEmployee = employeeRepository.save(employee);

        // Assert
        assertThat(savedEmployee.getDepartment()).isNotNull();
        assertThat(savedEmployee.getDepartment().getId()).isEqualTo(testDepartment.getId());
        assertThat(savedEmployee.getDepartment().getName()).isEqualTo("IT Department");
    }

    // Тест 14: Сохранение сотрудника с менеджером (иерархия)
    @Test
    @DisplayName("save() - should save employee with manager relationship")
    void save_ShouldSaveEmployeeWithManager_WhenManagerProvided() {
        // Arrange
        var manager = new Employee();
        manager.setName("Иван Менеджеров");
        manager.setPosition("Senior Manager");
        manager.setSalary(150000L);
        manager.setDepartment(testDepartment);
        var savedManager = employeeRepository.save(manager);

        var employee = new Employee();
        employee.setName("Петр Подчиненный");
        employee.setPosition("Junior Developer");
        employee.setSalary(60000L);
        employee.setDepartment(testDepartment);
        employee.setManager(savedManager);

        // Act
        var savedEmployee = employeeRepository.save(employee);

        // Assert
        assertThat(savedEmployee.getManager()).isNotNull();
        assertThat(savedEmployee.getManager().getId()).isEqualTo(savedManager.getId());
        assertThat(savedEmployee.getManager().getName()).isEqualTo("Иван Менеджеров");
    }

    // Тест 15: Комплексный тест - сохранение, обновление и удаление
    @Test
    @DisplayName("Complex test - save, update, and delete operations")
    void complexTest_ShouldPerformAllOperations_Successfully() {
        // 1. Создаем сотрудника
        var employee = new Employee();
        employee.setName("Максим Федоров");
        employee.setPosition("Software Architect");
        employee.setSalary(200000L);
        employee.setDepartment(testDepartment);
        var savedEmployee = employeeRepository.save(employee);

        assertThat(savedEmployee.getId()).isNotNull();
        assertThat(employeeRepository.count()).isEqualTo(1);

        // 2. Обновляем сотрудника
        savedEmployee.setSalary(220000L);
        savedEmployee.setPosition("Principal Architect");
        var updatedEmployee = employeeRepository.save(savedEmployee);

        assertThat(updatedEmployee.getSalary()).isEqualTo(220000L);
        assertThat(updatedEmployee.getPosition()).isEqualTo("Principal Architect");
        assertThat(employeeRepository.count()).isEqualTo(1);

        // 3. Проверяем что сотрудник существует
        assertThat(employeeRepository.existsById(updatedEmployee.getId())).isTrue();

        // 4. Удаляем сотрудника
        employeeRepository.deleteById(updatedEmployee.getId());

        // 5. Проверяем что сотрудник удален
        assertThat(employeeRepository.findById(updatedEmployee.getId())).isEmpty();
        assertThat(employeeRepository.count()).isZero();
        assertThat(employeeRepository.existsById(updatedEmployee.getId())).isFalse();
    }
}
