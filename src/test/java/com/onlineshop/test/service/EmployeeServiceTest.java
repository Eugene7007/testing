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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Spy
    EmployeeMapper employeeMapper;
    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    @Captor
    ArgumentCaptor<Employee> employeeCaptor;

    //
    @Test
    @DisplayName("getAllEmployees - Should return list of EmployeeResponse")
    void getAllEmployees_ShouldReturnListOfEmployeeResponses() {
        var emp1 = new Employee();
        var emp2 = new Employee();

        when(employeeRepository.findAll()).thenReturn(List.of(emp1, emp2));

        when(employeeMapper.toResponse(emp1))
                .thenReturn(new EmployeeResponse(1L, "Dilmurod ", "DevJava", BigDecimal.TEN, null, null));

        when(employeeMapper.toResponse(emp2))
                .thenReturn(new EmployeeResponse(2L, "Mamurjon", "DevQA", BigDecimal.ONE, null, null));

        var result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2);

        verify(employeeRepository).findAll();
        verify(employeeMapper, times(2)).toResponse(any());
    }

    //
    @Test
    @DisplayName("getAllEmployees - Should return empty list when no employees exist")
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesFound() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        var result = employeeService.getAllEmployees();

        assertThat(result).isEmpty();

        verify(employeeRepository).findAll();
        verify(employeeMapper, never()).toResponse(any());
    }

    //
    @Test
    @DisplayName("getEmployeeById - Should return EmployeeResponse when exists")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenExists() {
        var employeeId = 1L;
        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(1L, "Dima", "DevJAva", BigDecimal.TEN, null, null);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employeeEntity));
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        var result = employeeService.getEmployeeById(employeeId);

        assertThat(result).isNotNull().isEqualTo(employeeResponse);

        verify(employeeRepository).findById(employeeId);
        verify(employeeMapper).toResponse(employeeEntity);
    }

    //
    @Test
    @DisplayName("getEmployeeById - Should throw exception when not exists")
    void getEmployeeById_ShouldThrowException_WhenNotExists() {
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(employeeId));

        verify(employeeRepository).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    //
    @Test
    @DisplayName("createEmployee - Should save and return EmployeeResponse")
    void createEmployee_ShouldSaveAndReturnResponse() {
        var request = new EmployeeRequest("Azamat", "DevC#", BigDecimal.TEN, null, null);
        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(1L, "Azamat", "DevC#", BigDecimal.TEN, null, null);

        when(employeeMapper.toEntity(request)).thenReturn(employeeEntity);
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        var result = employeeService.createEmployee(request);

        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeMapper).toEntity(request);
        verify(employeeRepository).save(employeeEntity);
        verify(employeeMapper).toResponse(employeeEntity);
    }

    //
    @Test
    @DisplayName("createEmployee - Repository.save must be called exactly once")
    void createEmployee_ShouldCallRepositorySaveOnce() {
        var request = new EmployeeRequest("Tester", "QA", BigDecimal.TEN, null, null);
        var employee = new Employee();

        when(employeeMapper.toEntity(request)).thenReturn(employee);

        employeeService.createEmployee(request);

        verify(employeeRepository, times(1)).save(employee);
    }

    //
    @Test
    @DisplayName("updateEmployee - Should update and return EmployeeResponse")
    void updateEmployee_ShouldUpdateAndReturnResponse() {
        var employeeId = 1L;

        var existingEmployee = new Employee();
        existingEmployee.setName("Old");
        existingEmployee.setPosition("Old_Pos");
        existingEmployee.setSalary(BigDecimal.ONE);

        var request = new EmployeeRequest("Dilmurod", "Senior Backend Developer", BigDecimal.TEN, null, null);
        var updatedResponse = new EmployeeResponse(1L, "Dilmurod", "Senior Backend Developer", BigDecimal.TEN, null, null);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeMapper.toResponse(existingEmployee)).thenReturn(updatedResponse);

        var result = employeeService.updateEmployee(employeeId, request);

        assertThat(existingEmployee.getName()).isEqualTo("NewName");
        assertThat(existingEmployee.getPosition()).isEqualTo("NewPos");
        assertThat(existingEmployee.getSalary()).isEqualTo(BigDecimal.TEN);

        assertThat(result).isEqualTo(updatedResponse);

        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).save(existingEmployee);
        verify(employeeMapper).toResponse(existingEmployee);
    }

    //
    @Test
    @DisplayName("updateEmployee - Should throw exception when not exists")
    void updateEmployee_ShouldThrowException_WhenNotExists() {
        var id = 100L;
        var request = new EmployeeRequest("Ali", "DevJavA Backend", BigDecimal.ONE, null, null);

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.updateEmployee(id, request));

        verify(employeeRepository).findById(id);
        verifyNoInteractions(employeeMapper);
    }

    //
    @Test
    @DisplayName("deleteEmployee - Should delete when exists")
    void deleteEmployee_ShouldDelete_WhenExists() {
        var employeeId = 1L;
        var employee = new Employee();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).deleteById(employeeId);
    }

    //
    @Test
    @DisplayName("deleteEmployee - Should throw when not exists")
    void deleteEmployee_ShouldThrow_WhenNotExists() {
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.deleteEmployee(employeeId));

        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository, never()).deleteById(any());
    }
}