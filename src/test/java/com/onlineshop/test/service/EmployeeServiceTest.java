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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    EmployeeService employeeService;

    @Test
    void getAllEmployees_ReturnsList() {
        Department dep = new Department();
        Employee islam = new Employee(1L, "Islam", "Grapler", 200000L, dep, null);
        EmployeeResponse resp = new EmployeeResponse(1L, "Islam", "Grapler", 200000L, null, null);

        when(employeeRepository.findAll()).thenReturn(List.of(islam));
        when(employeeMapper.toResponse(islam)).thenReturn(resp);

        var result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("Islam", result.get(0).name());
    }

    @Test
    void getAllEmployees_Empty() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        var result = employeeService.getAllEmployees();

        assertTrue(result.isEmpty());
        verify(employeeRepository).findAll();
    }

    @Test
    void getEmployeeById_Exists() {
        Employee khabib = new Employee();
        EmployeeResponse resp = new EmployeeResponse(1L, "Khabib", "Wrestler", 300000L, null, null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(khabib));
        when(employeeMapper.toResponse(khabib)).thenReturn(resp);

        var result = employeeService.getEmployeeById(1L);

        assertEquals(resp, result);
    }

    @Test
    void getEmployeeById_NotFound() {
        when(employeeRepository.findById(22L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(22L));
    }

    @Test
    void createEmployee_Works() {
        EmployeeRequest req = new EmployeeRequest();
        req.setName("Jack");
        req.setPosition("Striker");
        req.setSalary(150000L);
        req.setDepartmentId(1L);

        Employee jack = new Employee();
        EmployeeResponse resp = new EmployeeResponse(10L, "Jack", "Striker", 150000L, null, null);

        when(employeeMapper.toEntity(req)).thenReturn(jack);
        when(employeeMapper.toResponse(jack)).thenReturn(resp);

        var result = employeeService.createEmployee(req);

        assertEquals(resp, result);
        verify(employeeRepository).save(jack);
    }

    @Test
    void updateEmployee_Works() {
        Department dep = new Department();
        var conor = new Employee(5L, "Conor", "Puncher", 120000L, dep, null);

        EmployeeRequest req = new EmployeeRequest();
        req.setName("Conor McGregor");
        req.setPosition("Striker");
        req.setSalary(200000L);
        req.setDepartmentId(1L);

        EmployeeResponse resp = new EmployeeResponse(5L, "Conor McGregor", "Striker", 200000L, null, null);

        when(employeeRepository.findById(5L)).thenReturn(Optional.of(conor));
        when(employeeMapper.toResponse(conor)).thenReturn(resp);

        var result = employeeService.updateEmployee(5L, req);

        assertEquals(resp, result);
        assertEquals("Conor McGregor", conor.getName());
        assertEquals(200000L, conor.getSalary());
    }

    @Test
    void updateEmployee_NotFound() {
        EmployeeRequest req = new EmployeeRequest();
        req.setName("Someone");

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.updateEmployee(99L, req));
    }

    @Test
    void deleteEmployee_Works() {
        Employee emp = new Employee();

        when(employeeRepository.findById(3L)).thenReturn(Optional.of(emp));

        assertDoesNotThrow(() -> employeeService.deleteEmployee(3L));
        verify(employeeRepository).deleteById(3L);
    }

    @Test
    void deleteEmployee_NotFound() {
        when(employeeRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.deleteEmployee(3L));
    }

    @Test
    void mapper_DefaultManagerName() {
        Employee islam = new Employee();
        islam.setManager(null);

        EmployeeResponse resp = new EmployeeResponse(1L, "Islam", "Grapler", 200000L, null, "Нет менеджера");
        when(employeeMapper.toResponse(islam)).thenReturn(resp);

        var result = employeeMapper.toResponse(islam);

        assertEquals("Нет менеджера", result.managerName());
    }

    @Test
    void createEmployee_MapperCalledOnce() {
        EmployeeRequest req = new EmployeeRequest();
        Employee ent = new Employee();
        EmployeeResponse resp = new EmployeeResponse(1L, "Islam", "Grapler", 100L, null, null);

        when(employeeMapper.toEntity(req)).thenReturn(ent);
        when(employeeMapper.toResponse(ent)).thenReturn(resp);

        employeeService.createEmployee(req);

        verify(employeeMapper, times(1)).toEntity(req);
        verify(employeeMapper, times(1)).toResponse(ent);
    }
}
