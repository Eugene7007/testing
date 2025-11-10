package com.onlineshop.test;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.repository.EmployeeRepository;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.service.EmployeeService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class TestApplicationTests {
    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    EmployeeService employeeService;

    private Employee employee_1 = new Employee();
    private Employee employee_2 = new Employee();

    private EmployeeResponse response_1 = new EmployeeResponse(1L, "Lebron", "SF", 10L, "Lakers", "JJ Redick");
    private EmployeeResponse response_2 = new EmployeeResponse(2L, "Luka", "PG", 5L, "Lakers", "JJ Redick");

    @Test
    public void getAllEmployee_ReturnMappedList() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee_1, employee_2));
        when(employeeMapper.toResponse(employee_1)).thenReturn(response_1);
        when(employeeMapper.toResponse(employee_2)).thenReturn(response_2);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Lebron", result.getFirst().name());

        verify(employeeRepository).findAll();
        verify(employeeMapper, times(2)).toResponse(any(Employee.class));
    }

    @Test
    public void getEmployeeById_ReturnEmployee () {
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee_2));
        when(employeeMapper.toResponse(employee_2)).thenReturn(response_2);

        EmployeeResponse result = employeeService.getEmployeeById(2L);

        Assertions.assertEquals("Luka", result.name());
        verify(employeeRepository).findById(2L);
        verify(employeeMapper).toResponse(employee_2);

    }

    @Test
    public void getEmployeeById_NotFound () {
        when(employeeRepository.findById(3L)).thenReturn(Optional.empty());
        Assertions.assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(3L));
        verify(employeeRepository).findById(3L);
    }

    @Test
    public void createEmployee_ReturnResponse () {
        EmployeeRequest request = new EmployeeRequest();
        Employee employee_3 = new Employee();
        EmployeeResponse response = new EmployeeResponse(3L, "Shai", "PG", 3L, "Thunders", "Someone");

        when(employeeMapper.toEntity(request)).thenReturn(employee_3);
        when(employeeMapper.toResponse(employee_3)).thenReturn(response);

        EmployeeResponse result = employeeService.createEmployee(request);

        verify(employeeRepository).save(employee_3);
        Assertions.assertEquals("Shai", result.name());
    }

    @Test
    public void updateEmployee_UpdateAndReturnResponse () {
        Long id = 1L;

        Employee existing = new Employee();
        existing.setId(id);
        existing.setName("Lebron");
        existing.setPosition("SF");
        existing.setSalary(10L);

        EmployeeRequest request = new EmployeeRequest();
        request.setName("Carmelo");
        request.setPosition("SF");
        request.setSalary(7L);

        EmployeeResponse expectedResponse = new EmployeeResponse(3L , "Carmelo" , "SF" , 7L, "Nuggets" , "Someone");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(existing)).thenReturn(existing);
        when(employeeMapper.toResponse(existing)).thenReturn(expectedResponse);

        EmployeeResponse actualResponse = employeeService.updateEmployee(id, request);

        Assertions.assertEquals(expectedResponse.name(), actualResponse.name());
        Assertions.assertEquals(expectedResponse.position(), actualResponse.position());
        Assertions.assertEquals(expectedResponse.salary(), actualResponse.salary());

        verify(employeeRepository).findById(id);
        verify(employeeRepository).save(existing);
        verify(employeeMapper).toResponse(existing);
    }

    @Test
    public void updateEmployee_NotFound () {
        Long id = 4L;
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Nikola");
        request.setPosition("C");
        request.setSalary(8L);

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(id, request));

        verify(employeeRepository).findById(id);
        verify(employeeRepository, never()).save(any());
        verify(employeeMapper, never()).toResponse(any());
    }

    @Test
    public void deleteEmployee_DeleteEmployee () {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee_1));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).deleteById(1L);
    }

    @Test
    public void deleteEmployee_NotFound () {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(1L));
        verify(employeeRepository).findById(1L);
    }
}