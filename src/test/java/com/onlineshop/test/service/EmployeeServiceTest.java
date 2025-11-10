package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    EmployeeMapper employeeMapper;
    @InjectMocks
    EmployeeService employeeService;
    @Captor
    ArgumentCaptor<Employee> empCaptor;

    @Test
    void getAllEmployees_ShouldReturnAllEmployeesResponse_WhenListOfEmployeesExists() {
        Department department = new Department(1L, "SOmeThing", "New-york");

        Employee manager = new Employee(9L, "man1", "manager", 105000L, department, null);
        Employee e1 = new Employee(1L, "emp1", "developer", 15000L, department, manager);
        Employee e2 = new Employee(2L, "emp2", "hr", 99000L, department, manager);

        EmployeeResponse employeeResponse1 = new EmployeeResponse(e1.getId(), e1.getName(), e1.getPosition(), e1.getSalary(), e1.getDepartment().getName(), e1.getManager().getName());
        EmployeeResponse employeeResponse2 = new EmployeeResponse(e2.getId(), e2.getName(), e2.getPosition(), e2.getSalary(), e2.getDepartment().getName(), e2.getManager().getName());

        List<EmployeeResponse> responseList = new ArrayList<>();
        responseList.add(employeeResponse1);
        responseList.add(employeeResponse2);

        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

        when(employeeMapper.toResponse(e1)).thenReturn(employeeResponse1);
        when(employeeMapper.toResponse(e2)).thenReturn(employeeResponse2);

        var result = employeeService.getAllEmployees();

        assertThat(result).isNotNull();
        assertThat(result).size().isEqualTo(responseList.size());

        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(e1);
        verify(employeeMapper, times(2)).toResponse(empCaptor.capture());

    }

    @Test
    void getAllEmployees_ShouldReturnEmptyList_WhenListOfEmployeesDoesNotExist() {

        when(employeeRepository.findAll()).thenReturn(List.of());

        var result = employeeService.getAllEmployees();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(employeeRepository, times(1)).findAll();
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        var employeeId = 1L;
        var employee = new Employee();
        var employeeResponse = new EmployeeResponse(1L, "Bob", "developer", 1500L, "IT", "Alice");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var result = employeeService.getEmployeeById(employeeId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employee);
        verify(employeeMapper, times(1)).toResponse(empCaptor.capture());
    }


    @Test
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists_ButManagerDoesNotExist() {

        var employeeId = 1L;
        var department = new Department(1L, "SOmeThing", "New-york");
        var employee = new Employee(1L, "Bob", "developer", 1500L, department, null);
        var employeeResponse = new EmployeeResponse(1L, "Bob", "developer", 1500L, "IT", null);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var result = employeeService.getEmployeeById(employeeId);

        assertThat(result).isNotNull();
        assertThat(result.managerName()).isNull();
        assertThat(result).isEqualTo(employeeResponse);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employee);
        verify(employeeMapper, times(1)).toResponse(empCaptor.capture());
    }

    @Test
    void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void creteEmployee_ShouldSaveAndReturnEmployeeResponse_WhenEmployeeExists() {
        var department = new Department(1L, "SOmeThing", "New-york");
        var req = new EmployeeRequest();
        req.setName("Bob");
        req.setPosition("IT");
        req.setSalary(10000L);
        req.setDepartmentId(department.getId());
        req.setManagerId(null);

        var employee = new Employee(1L, req.getName(), "developer", req.getSalary(), department, null);
        var response = new EmployeeResponse(employee.getId(), employee.getName(), employee.getPosition(), employee.getSalary(), department.getName(), null);

        when(employeeMapper.toEntity(req)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        var result = employeeService.createEmployee(req);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);


    }

    @Test
    void updateEmployee_ShouldUpdateAnReturnEmployeeResponse_WhenEmployeeExists() {
        var employeeId = 1L;
        var department = new Department(1L, "SOmeThing", "New-york");
        var existingEmployee = new Employee(1L, "Bob", "developer", 10000L, department, null);

        var request = new EmployeeRequest();
        request.setName("Bob");
        request.setPosition("Middle developer");
        request.setSalary(15000L);
        request.setDepartmentId(department.getId());
        request.setManagerId(null);

        var response = new EmployeeResponse(employeeId, "Bob", "Middle developer", 15000L, "SOmeThing", null);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeMapper.toResponse(existingEmployee)).thenReturn(response);
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);

        var result = employeeService.updateEmployee(employeeId, request);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);

    }

    @Test
    void updateEmployee_ShouldUpdateDepartmentAndReturnEmployeeResponse_WhenEmployeeExists() {
        var employeeId = 1L;
        var department = new Department(1L, "SOmeThing", "New-york");
        var newDepartment = new Department(2L, "Cross-boarder", "California");

        var existingEmployee = new Employee(1L, "Bob", "developer", 10000L, department, null);

        var request = new EmployeeRequest();
        request.setName("Bob");
        request.setPosition("Middle developer");
        request.setSalary(15000L);
        request.setDepartmentId(newDepartment.getId());
        request.setManagerId(null);

        var response = new EmployeeResponse(employeeId, "Bob", "Middle developer", 15000L, "Cross-boarder", null);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeMapper.toResponse(existingEmployee)).thenReturn(response);
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);

        var result = employeeService.updateEmployee(existingEmployee.getId(), request);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);
    }

    @Test
    void updateEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        var employeeId = 1L;
        var department = new Department(1L, "SOmeThing", "New-york");
        var request = new EmployeeRequest();
        request.setName("Bob");
        request.setPosition("Middle developer");
        request.setPosition("IT");
        request.setSalary(15000L);
        request.setDepartmentId(department.getId());
        request.setManagerId(null);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(employeeId, request));

        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);

    }

    @Test
    void deleteEmployee_ShouldDeleteEmployeeAndVoid_WhenEmployeeExists() {
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(new Employee()));

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void deleteEmployee_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(0)).deleteById(employeeId);

        verifyNoInteractions(employeeMapper);
    }


}
