package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.DepartmentNotFoundException;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @DisplayName("getById")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists(){
        var employeeId = 1L;

        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(1L, "Name1", "Position1", 100L, "Department1", "Manager01");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employeeEntity));
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);

        var result = employeeService.getEmployeeById(employeeId);

        assertThat(result).isNotNull();
        assertThat(employeeResponse)
                .isNotNull()
                .isEqualTo(result);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
        verify(employeeMapper, times(1)).toResponse(employeeCaptor.capture());
    }

    @Test
    void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist(){
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void getAllEmployees_ShouldReturnAllEmployees_WhenEmployeesExist(){
        LinkedList<Employee> employeeList = new LinkedList<Employee>();
        employeeList.add(new Employee(1l, "Test 1", "Pos 1", 100l, new Department(), new Employee()));
        employeeList.add(new Employee(2l, "Test 2", "Pos 2", 100l, new Department(), new Employee()));
        when(employeeRepository.findAll()).thenReturn(employeeList);
        List<EmployeeResponse> resp = employeeService.getAllEmployees();
        verify(employeeRepository).findAll();
        assertEquals(resp.size(), employeeList.size());
    }

    @Test
    void createEmployee_ShouldCreateEmployee_WhenEmployeeDoesNotExist(){
        Employee employeeEntity = new Employee();
        EmployeeResponse employeeResponse = new EmployeeResponse(2L,"Name", "Pos",400L, "Department", "Manager");
        when(employeeMapper.toResponse(employeeEntity)).thenReturn(employeeResponse);
        employeeRepository.save(employeeEntity);
        EmployeeResponse result = employeeMapper.toResponse(employeeEntity);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);
    }

    @Test
    void deleteEmployee_ShouldDeleteEmployee_WhenEmployeeExists(){
        Long employeeId = 1L;
        Employee employee = new Employee();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        employeeService.deleteEmployee(employeeId);
        verify(employeeRepository).deleteById(employeeId);
    }

    @Test
    void updateEmployee_ShouldUpdateEmployee_WhenEmployeeExists(){
        Long employeeId = 1L;
        Employee employee = new Employee();
        EmployeeRequest employeeRequest = new EmployeeRequest();
        EmployeeResponse employeeResponse = new EmployeeResponse(1L,"Name", "Pos", 100L,"Depart","Manger");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.updateEmployee(employeeId, employeeRequest);

        assertNotNull(result);
        assertEquals(result, employeeResponse);

        verify(employeeRepository,times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void updateEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist(){
        Long employeeId = 1L;
        EmployeeRequest req = new EmployeeRequest();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, ()-> employeeService.updateEmployee(employeeId, req));
    }

    @Test
    void deleteEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist(){
        Long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, ()-> employeeService.deleteEmployee(employeeId));
    }

}
