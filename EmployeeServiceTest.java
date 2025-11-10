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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Spy
    EmployeeMapper employeeMapper;

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

// 1 return employee in getEmployeeById
    @Test
    @DisplayName("getEmployeeById — возвращает сотрудника, если найден")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        Long employeeId = 1L;
        Employee employeeEntity = new Employee();
        employeeEntity.setId(employeeId);
        employeeEntity.setName("Alice");
        employeeEntity.setPosition("Manager");
        employeeEntity.setSalary(5000L);

        EmployeeResponse employeeResponse = new EmployeeResponse(
                1L, "Alice", "Manager", 5000L, null, null
        );

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employeeEntity));
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getEmployeeById(employeeId);

        assertThat(result).isNotNull().isEqualTo(employeeResponse);
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

// 2 если нету сотрудника в getEmployeeById
    @Test
    @DisplayName("getEmployeeById — исключение, если сотрудника нету")
    void getEmployeeById_ShouldThrowException_WhenEmployeeDoesNotExist() {
        Long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

// 3 список всех сотрудников getAllEmployees
    @Test
    @DisplayName("getAllEmployees — возвращает список всех сотрудников")
    void getAllEmployees_ShouldReturnList_WhenEmployeesExist() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "Alice", "Manager", 5000L, null, null),
                new Employee(2L, "Bob", "Developer", 4000L, null, null)
        );

        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.toResponse(any(Employee.class)))
                .thenAnswer(invocation -> {
                    Employee e = invocation.getArgument(0);
                    return new EmployeeResponse(e.getId(), e.getName(), e.getPosition(), e.getSalary(), null, null);
                });

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2);
        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(2)).toResponse(any(Employee.class));
    }

// 4 если нету никого в getALlEmployees
    @Test
    @DisplayName("getAllEmployees — пустой список, если сотрудников нет")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExist() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(employeeRepository, times(1)).findAll();
        verifyNoInteractions(employeeMapper);
    }

// 5 сохранение нового сотрудника в createEmployee
    @Test
    @DisplayName("createEmployee — сохраняет нового сотрудника")
    void createEmployee_ShouldSaveAndReturnResponse() {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Lance");
        request.setPosition("Manager");
        request.setSalary(5000L);

        Employee entity = new Employee(null, "Lance", "Manager", 5000L, null, null);
        Employee saved = new Employee(1L, "Lance", "Manager", 5000L, null, null);
        EmployeeResponse response = new EmployeeResponse(1L, "Lance", "Manager", 5000L, null, null);

        when(employeeMapper.toEntity(request)).thenReturn(entity);
        when(employeeRepository.save(entity)).thenReturn(saved);
        when(employeeMapper.toResponse(saved)).thenReturn(response);

        EmployeeResponse result = employeeService.createEmployee(request);

        assertThat(result).isEqualTo(response);
        verify(employeeMapper).toEntity(request);
        verify(employeeRepository).save(entity);
        verify(employeeMapper).toResponse(saved);
    }

//  6 если null в createEmployee
    @Test
    @DisplayName("createEmployee — IllegalArgumentException при null")
    void createEmployee_ShouldThrowException_WhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
        verifyNoInteractions(employeeRepository, employeeMapper);
    }

//  7 обновление данных сотрудника
    @Test
    @DisplayName("updateEmployee — обновляет данные существующего сотрудника")
    void updateEmployee_ShouldUpdateExistingEmployee() {
        Long id = 1L;
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Lance");
        request.setPosition("Manager");
        request.setSalary(5000L);

        Employee existing = new Employee(1L, "Lance", "Manager", 5000L, null, null);
        Employee updated = new Employee(1L, "Lance", "Director", 6000L, null, null);
        EmployeeResponse response = new EmployeeResponse(1L, "Lance", "Director", 6000L, null, null);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        when(employeeMapper.toResponse(updated)).thenReturn(response);

        EmployeeResponse result = employeeService.updateEmployee(id, request);

        assertThat(result).isEqualTo(response);
        verify(employeeRepository).findById(id);
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeMapper).toResponse(updated);
    }

// 8 исключение если нету сотрудника
    @Test
    @DisplayName("updateEmployee — исключение, если сотрудника нету")
    void updateEmployee_ShouldThrowException_WhenEmployeeNotFound() {
        Long id = 99L;
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Lance");
        request.setPosition("Manager");
        request.setSalary(6000L);


        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(id, request));
        verify(employeeRepository).findById(id);
        verify(employeeRepository, never()).save(any());
    }

//  9 удаляет найденного сотрудника
    @Test
    @DisplayName("deleteEmployee — удаляет сотрудника, если найден")
    void deleteEmployee_ShouldDelete_WhenEmployeeExists() {
        Long id = 1L;
        Employee employee = new Employee(id, "Lance", "Manager", 5000L, null, null);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(id);

        verify(employeeRepository).findById(id);
        verify(employeeRepository).delete(employee);
    }

//  10 исключение, если нету сотрудника
    @Test
    @DisplayName("deleteEmployee — исключение, если сотрудника нету")
    void deleteEmployee_ShouldThrowException_WhenEmployeeNotFound() {
        Long id = 1L;
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(id));
        verify(employeeRepository).findById(id);
        verify(employeeRepository, never()).delete(any());
    }

//    11 исключение при попытке удаления сотрудника которого нету
@Test
@DisplayName("deleteEmployee — исключение, если пытаемся удалить несуществующего сотрудника")
void deleteEmployee_ShouldThrowException_WhenEmployeeDoesNotExist() {
    Long id = 999L;
    when(employeeRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(id));

    verify(employeeRepository, times(1)).findById(id);
    verify(employeeRepository, never()).delete(any());
}
}
