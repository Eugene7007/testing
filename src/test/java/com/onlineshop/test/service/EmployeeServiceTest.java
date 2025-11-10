package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Unit tests для EmployeeService
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Spy
    EmployeeMapper employeeMapper;

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    @Captor
    ArgumentCaptor<Employee> employeeCaptor;

    // Тест 1: Проверяем что возвращается пустой список если сотрудников нет
    @Test
    @DisplayName("getAllEmployees - пустой список")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExist() {
        // Arrange - готовим данные
        when(employeeRepository.findAll())
            .thenReturn(Collections.emptyList());

        // Act - выполняем тест
        var result = employeeService.getAllEmployees();

        // Assert - проверяем результат
        assertThat(result).isEmpty();

        // Verify - проверяем что методы вызвались
        verify(employeeRepository, times(1)).findAll();
    }

    // Тест 2: Проверяем что возвращается список сотрудников
    @Test
    @DisplayName("getAllEmployees - список с данными")
    void getAllEmployees_ShouldReturnEmployeesList_WhenEmployeesExist() {
        // Arrange
        var employee1 = new Employee();
        employee1.setId(1L);
        employee1.setName("Иван");

        var employee2 = new Employee();
        employee2.setId(2L);
        employee2.setName("Петр");

        var employeeResponse1 = new EmployeeResponse(1L, "Иван", "Developer", 50000L, "IT", null);
        var employeeResponse2 = new EmployeeResponse(2L, "Петр", "Manager", 70000L, "HR", null);

        when(employeeRepository.findAll())
            .thenReturn(List.of(employee1, employee2));

        when(employeeMapper.toResponse(employee1))
            .thenReturn(employeeResponse1);

        when(employeeMapper.toResponse(employee2))
            .thenReturn(employeeResponse2);

        // Act
        var result = employeeService.getAllEmployees();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(employeeResponse1, employeeResponse2);

        // Verify
        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(2)).toResponse(any(Employee.class));
    }

    // Тест 3: Проверяем что находим сотрудника по ID
    @Test
    @DisplayName("getById - находим сотрудника")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        // Arrange
        var employeeId = 1L;
        var employee = new Employee();
        employee.setId(employeeId);
        employee.setName("Иван");

        var employeeResponse = new EmployeeResponse(1L, "Иван", "Developer", 50000L, "IT", null);

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(employee));

        when(employeeMapper.toResponse(employee))
            .thenReturn(employeeResponse);

        // Act
        var result = employeeService.getEmployeeById(employeeId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employee);
    }

    // Тест 4: Проверяем что выбрасывается исключение если сотрудник не найден
    @Test
    @DisplayName("getById - сотрудник не найден")
    void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        // Arrange
        var employeeId = 999L;

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.empty());

        // Act + Assert - проверяем что выбросилось исключение
        assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.getEmployeeById(employeeId));

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    // Тест 5: Проверяем создание сотрудника
    @Test
    @DisplayName("createEmployee - успешное создание")
    void createEmployee_ShouldReturnEmployeeResponse_WhenEmployeeCreated() {
        // Arrange
        var request = new EmployeeRequest();
        request.setName("Иван");
        request.setPosition("Developer");
        request.setSalary(50000L);
        request.setDepartmentId(1L);

        var employee = new Employee();
        employee.setId(1L);
        employee.setName("Иван");
        employee.setPosition("Developer");
        employee.setSalary(50000L);

        var employeeResponse = new EmployeeResponse(1L, "Иван", "Developer", 50000L, "IT", null);

        when(employeeMapper.toEntity(request))
            .thenReturn(employee);

        when(employeeMapper.toResponse(employee))
            .thenReturn(employeeResponse);

        when(employeeRepository.save(employee))
            .thenReturn(employee);

        // Act
        var result = employeeService.createEmployee(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        // Verify
        verify(employeeMapper, times(1)).toEntity(request);
        verify(employeeRepository, times(1)).save(employee);
        verify(employeeMapper, times(1)).toResponse(employee);
    }

    // Тест 6: Проверяем что сотрудник сохраняется в репозиторий
    @Test
    @DisplayName("createEmployee - сохранение в репозиторий")
    void createEmployee_ShouldSaveEmployeeToRepository_WhenEmployeeCreated() {
        // Arrange
        var request = new EmployeeRequest();
        request.setName("Петр");
        request.setPosition("Manager");
        request.setSalary(70000L);

        var employee = new Employee();
        employee.setName("Петр");

        when(employeeMapper.toEntity(request))
            .thenReturn(employee);

        when(employeeMapper.toResponse(employee))
            .thenReturn(new EmployeeResponse(1L, "Петр", "Manager", 70000L, "HR", null));

        // Act
        employeeService.createEmployee(request);

        // Assert + Verify - проверяем что save вызвался с нужным объектом
        verify(employeeRepository, times(1)).save(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue()).isEqualTo(employee);
    }

    // Тест 7: Проверяем маппинг при создании
    @Test
    @DisplayName("createEmployee - маппинг request в entity")
    void createEmployee_ShouldMapRequestToEntity_WhenCreatingEmployee() {
        // Arrange
        var request = new EmployeeRequest();
        request.setName("Анна");
        request.setPosition("Designer");
        request.setSalary(60000L);

        var employee = new Employee();

        when(employeeMapper.toEntity(request))
            .thenReturn(employee);

        when(employeeMapper.toResponse(any(Employee.class)))
            .thenReturn(new EmployeeResponse(1L, "Анна", "Designer", 60000L, "Design", null));

        // Act
        employeeService.createEmployee(request);

        // Verify - проверяем что маппер вызвался
        verify(employeeMapper, times(1)).toEntity(request);
    }

    // Тест 8: Проверяем обновление сотрудника
    @Test
    @DisplayName("updateEmployee - успешное обновление")
    void updateEmployee_ShouldReturnUpdatedEmployeeResponse_WhenEmployeeExists() {
        // Arrange
        var employeeId = 1L;
        var request = new EmployeeRequest();
        request.setName("Иван Обновленный");
        request.setPosition("Senior Developer");
        request.setSalary(80000L);

        var existingEmployee = new Employee();
        existingEmployee.setId(employeeId);
        existingEmployee.setName("Иван");
        existingEmployee.setPosition("Developer");
        existingEmployee.setSalary(50000L);

        var updatedResponse = new EmployeeResponse(1L, "Иван Обновленный", "Senior Developer", 80000L, "IT", null);

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(existingEmployee));

        when(employeeMapper.toResponse(existingEmployee))
            .thenReturn(updatedResponse);

        // Act
        var result = employeeService.updateEmployee(employeeId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedResponse);

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(existingEmployee);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);
    }

    // Тест 9: Проверяем что выбрасывается исключение при обновлении несуществующего сотрудника
    @Test
    @DisplayName("updateEmployee - сотрудник не найден")
    void updateEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        // Arrange
        var employeeId = 999L;
        var request = new EmployeeRequest();

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.updateEmployee(employeeId, request));

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(0)).save(any(Employee.class));
    }

    // Тест 10: Проверяем что поля сотрудника обновляются
    @Test
    @DisplayName("updateEmployee - обновление полей")
    void updateEmployee_ShouldUpdateEmployeeFields_WhenEmployeeExists() {
        // Arrange
        var employeeId = 1L;
        var request = new EmployeeRequest();
        request.setName("Новое Имя");
        request.setPosition("Новая Должность");
        request.setSalary(100000L);

        var existingEmployee = new Employee();
        existingEmployee.setId(employeeId);
        existingEmployee.setName("Старое Имя");
        existingEmployee.setPosition("Старая Должность");
        existingEmployee.setSalary(50000L);

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(existingEmployee));

        when(employeeMapper.toResponse(any(Employee.class)))
            .thenReturn(new EmployeeResponse(1L, "Новое Имя", "Новая Должность", 100000L, "IT", null));

        // Act
        employeeService.updateEmployee(employeeId, request);

        // Assert - проверяем что поля обновились
        assertThat(existingEmployee.getName()).isEqualTo("Новое Имя");
        assertThat(existingEmployee.getPosition()).isEqualTo("Новая Должность");
        assertThat(existingEmployee.getSalary()).isEqualTo(100000L);

        // Verify
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    // Тест 11: Проверяем что обновленный сотрудник сохраняется
    @Test
    @DisplayName("updateEmployee - сохранение обновлений")
    void updateEmployee_ShouldSaveUpdatedEmployee_WhenEmployeeExists() {
        // Arrange
        var employeeId = 1L;
        var request = new EmployeeRequest();
        request.setName("Обновленный");
        request.setPosition("Updated Position");
        request.setSalary(90000L);

        var existingEmployee = new Employee();
        existingEmployee.setId(employeeId);

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(existingEmployee));

        when(employeeMapper.toResponse(any(Employee.class)))
            .thenReturn(new EmployeeResponse(1L, "Обновленный", "Updated Position", 90000L, "IT", null));

        // Act
        employeeService.updateEmployee(employeeId, request);

        // Verify - проверяем что save вызвался с правильным объектом
        verify(employeeRepository, times(1)).save(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue()).isEqualTo(existingEmployee);
    }

    // Тест 12: Проверяем удаление сотрудника
    @Test
    @DisplayName("deleteEmployee - успешное удаление")
    void deleteEmployee_ShouldDeleteEmployee_WhenEmployeeExists() {
        // Arrange
        var employeeId = 1L;
        var employee = new Employee();
        employee.setId(employeeId);

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(employee));

        // Act
        employeeService.deleteEmployee(employeeId);

        // Verify - проверяем что методы вызвались
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    // Тест 13: Проверяем что выбрасывается исключение при удалении несуществующего сотрудника
    @Test
    @DisplayName("deleteEmployee - сотрудник не найден")
    void deleteEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        // Arrange
        var employeeId = 999L;

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.deleteEmployee(employeeId));

        // Verify - проверяем что deleteById не вызвался
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(0)).deleteById(employeeId);
    }

    // Тест 14: Проверяем что возвращается правильное количество сотрудников
    @Test
    @DisplayName("getAllEmployees - правильное количество")
    void getAllEmployees_ShouldReturnCorrectNumberOfEmployees_WhenMultipleEmployeesExist() {
        // Arrange
        var employee1 = new Employee();
        var employee2 = new Employee();
        var employee3 = new Employee();

        when(employeeRepository.findAll())
            .thenReturn(List.of(employee1, employee2, employee3));

        when(employeeMapper.toResponse(any(Employee.class)))
            .thenReturn(new EmployeeResponse(1L, "Test", "Position", 50000L, "IT", null));

        // Act
        var result = employeeService.getAllEmployees();

        // Assert - проверяем что вернулось 3 сотрудника
        assertThat(result).hasSize(3);

        // Verify
        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(3)).toResponse(any(Employee.class));
    }

    // Тест 15: Проверяем что при создании вызываются нужные методы
    @Test
    @DisplayName("createEmployee - вызов маппера и репозитория")
    void createEmployee_ShouldCallMapperAndRepository_WhenCreatingEmployee() {
        // Arrange
        var request = new EmployeeRequest();
        request.setName("Тест");
        request.setPosition("Tester");
        request.setSalary(55000L);

        var employee = new Employee();

        when(employeeMapper.toEntity(request))
            .thenReturn(employee);

        when(employeeMapper.toResponse(employee))
            .thenReturn(new EmployeeResponse(1L, "Тест", "Tester", 55000L, "QA", null));

        // Act
        employeeService.createEmployee(request);

        // Verify - проверяем что все нужные методы вызвались по 1 разу
        verify(employeeMapper, times(1)).toEntity(request);
        verify(employeeRepository, times(1)).save(employee);
        verify(employeeMapper, times(1)).toResponse(employee);
    }
}
