package com.onlineshop.test.service;


import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


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


    @Test
    @DisplayName("getAllEmployees")
    void getAllEmployees_ShouldReturnListOfResponses_WhenEmployeesExist() {
        // Arrange
        var entity1 = new Employee();
        var entity2 = new Employee();
        var response1 = new EmployeeResponse(1L, "John", "Developer", 5000L, null, null);
        var response2 = new EmployeeResponse(2L, "Jane", "HR", 7000L, null, null);
        when(employeeRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(employeeMapper.toResponse(entity1)).thenReturn(response1);
        when(employeeMapper.toResponse(entity2)).thenReturn(response2);

        // Act
        var result = employeeService.getAllEmployees();

        // Assert
        assertThat(result)
            .isNotNull()
            .hasSize(2)
            .containsExactly(response1, response2);

        // Verify
        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(entity1);
        verify(employeeMapper, times(1)).toResponse(entity2);
    }

    @Test
    @DisplayName("getAllEmployees - empty")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployees() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(List.of());

        // Act
        var result = employeeService.getAllEmployees();

        // Assert
        assertThat(result).isNotNull().isEmpty();

        // Verify
        verify(employeeRepository, times(1)).findAll();
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("getById")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        // Arrange
        var employeeId = 1L;
        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(1L, "John", "Developer", 5000L, null, null);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employeeEntity));
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        // Act
        var result = employeeService.getEmployeeById(employeeId);

        // Assert
        assertThat(result).isNotNull().isEqualTo(employeeResponse);

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
    }

    @Test
    @DisplayName("getById - not found")
    void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        // Arrange
        var employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("create")
    void createEmployee_ShouldSaveAndReturnResponse_WhenRequestIsValid() {
        // Arrange
        var request = new EmployeeRequest("John", "Developer", 5000L, 1L, null);
        var entity = new Employee();
        var savedEntity = new Employee();
        savedEntity.setId(1L);
        var response = new EmployeeResponse(1L, "John", "Developer", 5000L, null, null);

        when(employeeMapper.toEntity(request)).thenReturn(entity);
        when(employeeRepository.save(entity)).thenReturn(savedEntity);
        when(employeeMapper.toResponse(entity)).thenReturn(response);

        // Act
        var result = employeeService.createEmployee(request);

        // Assert
        assertThat(result).isNotNull().isEqualTo(response);

        // Verify
        verify(employeeMapper, times(1)).toEntity(request);
        verify(employeeRepository, times(1)).save(employeeCaptor.capture());
        verify(employeeMapper, times(1)).toResponse(entity);
        assertThat(employeeCaptor.getValue()).isSameAs(entity);
    }

    @Test
    @DisplayName("update")
    void updateEmployee_ShouldUpdateFieldsAndReturnResponse_WhenEmployeeExists() {
        // Arrange
        var employeeId = 1L;
        var existing = new Employee();
        existing.setId(employeeId);
        existing.setName("Old");
        existing.setPosition("OldPos");
        existing.setSalary(1000L);

        var request = new EmployeeRequest("Ted", "Teach", 9999L, null, null);
        var updatedResponse = new EmployeeResponse(employeeId, "Ted", "Teach", 9999L, null, null);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(existing)).thenReturn(existing);
        when(employeeMapper.toResponse(existing)).thenReturn(updatedResponse);

        // Act
        var result = employeeService.updateEmployee(employeeId, request);

        // Assert
        assertThat(result).isNotNull().isEqualTo(updatedResponse);
        assertThat(existing.getName()).isEqualTo("Ted");
        assertThat(existing.getPosition()).isEqualTo("Teach");
        assertThat(existing.getSalary()).isEqualTo(9999L);

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(existing);
        verify(employeeMapper, times(1)).toResponse(existing);
        verifyNoMoreInteractions(employeeMapper);
    }

    @Test
    @DisplayName("update - not found")
    void updateEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        // Arrange
        var employeeId = 1L;
        var request = new EmployeeRequest("Name", "Pos", 5000L, null, null);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(employeeId, request));

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    @DisplayName("delete")
    void deleteEmployee_ShouldDelete_WhenEmployeeExists() {
        // Arrange
        var employeeId = 1L;
        var employee = new Employee();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // Act
        employeeService.deleteEmployee(employeeId);

        // Assert (no exception thrown)

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("delete - not found")
    void deleteEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        // Arrange
        var employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));

        // Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).deleteById(any());
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("update - department and manager not changed")
    void updateEmployee_ShouldPreserveDepartmentAndManager_WhenNotProvidedInRequest() {
        // Arrange
        var employeeId = 1L;
        var department = new Department();
        var manager = new Employee();
        var existing = new Employee();

        existing.setId(employeeId);
        existing.setDepartment(department);
        existing.setManager(manager);

        var request = new EmployeeRequest("JonSnow", "KingInTheNorth", 6000L, null, null);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(existing)).thenReturn(existing);
        when(employeeMapper.toResponse(existing)).thenReturn(new EmployeeResponse(employeeId, "JonSnow", "KingInTheNorth", 6000L, null, null));

        // Act
        employeeService.updateEmployee(employeeId, request);

        // Assert
        assertThat(existing.getDepartment()).isSameAs(department);
        assertThat(existing.getManager()).isSameAs(manager);

        // Verify
        verify(employeeRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("create - mapper called correctly")
    void createEmployee_ShouldCallMapperAndSave_WhenRequestIsProvided() {
        // Arrange
        var request = new EmployeeRequest("John", "Dev", 5000L, null, null);
        var entity = new Employee();
        var saved = new Employee();
        saved.setId(1L);
        when(employeeMapper.toEntity(request)).thenReturn(entity);
        when(employeeRepository.save(entity)).thenReturn(saved);
        when(employeeMapper.toResponse(entity)).thenReturn(new EmployeeResponse(1L, "John", "Dev", 5000L, null, null));

        // Act
        employeeService.createEmployee(request);

        // Verify
        verify(employeeMapper).toEntity(request);
        verify(employeeRepository).save(employeeCaptor.capture());
        verify(employeeMapper).toResponse(entity);
        assertThat(employeeCaptor.getValue()).isSameAs(entity);
    }
}
