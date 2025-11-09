package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.DepartmentNotFoundException;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.DepartmentRepository;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceAdditionalTest {

    @Spy
    EmployeeMapper employeeMapper;

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    DepartmentRepository departmentRepository;

    @InjectMocks
    EmployeeService employeeService;

    @Captor
    ArgumentCaptor<Employee> employeeCaptor;

    @Test
    @DisplayName("Return list when repository has employees")
    void getAllEmployees_ShouldReturnList() {
        var dep = new Department(1L, "IT", "Tashkent");
        var manager = new Employee(1L, "Tom", "Manager", 1000L, dep, null);
        var e1 = new Employee(2L, "Bob", "Developer", 500L, dep, manager);
        var e2 = new Employee(3L, "Alice", "Tester", 600L, dep, manager);
        var r1 = new EmployeeResponse(2L, "Bob", "Developer", 500L, "IT", "Tom");
        var r2 = new EmployeeResponse(3L, "Alice", "Tester", 600L, "IT", "Tom");

        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));
        when(employeeMapper.toResponse(e1)).thenReturn(r1);
        when(employeeMapper.toResponse(e2)).thenReturn(r2);

        var result = employeeService.getAllEmployees();

        assertThat(result).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("Return empty list when no employees")
    void getAllEmployees_ShouldReturnEmptyList() {
        when(employeeRepository.findAll()).thenReturn(List.of());
        var result = employeeService.getAllEmployees();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Update employee successfully when exists")
    void updateEmployee_ShouldUpdate_WhenExists() {
        var dep = new Department(1L, "IT", "Tashkent");
        var manager = new Employee(2L, "Boss", "Manager", 1200L, dep, null);
        var existing = new Employee(1L, "Old", "Intern", 500L, dep, manager);

        var req = new EmployeeRequest();
        req.setName("NewName");
        req.setPosition("Developer");
        req.setSalary(800L);
        req.setDepartmentId(1L);
        req.setManagerId(2L);

        var resp = new EmployeeResponse(1L, "NewName", "Developer", 800L, "IT", "Boss");

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(existing));
        when(employeeRepository.save(existing)).thenReturn(existing);
        when(employeeMapper.toResponse(existing)).thenReturn(resp);

        var result = employeeService.updateEmployee(1L, req);

        assertThat(result).isEqualTo(resp);

        verify(employeeRepository).findById(anyLong());
        verify(employeeRepository).save(existing);
        verify(employeeMapper).toResponse(existing);

        verifyNoInteractions(departmentRepository);
    }




    @Test
    @DisplayName("Should set department as null when Department not found")
    void updateEmployee_ShouldSetDepartmentNull_WhenDepartmentMissing() {
        var req = new EmployeeRequest();
        req.setDepartmentId(1L);
        var emp = new Employee();

        lenient().when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        lenient().when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        lenient().when(employeeRepository.save(emp)).thenReturn(emp);
        lenient().when(employeeMapper.toResponse(emp))
                .thenReturn(new EmployeeResponse(1L, "Bob", "Dev", 800L, null, null));

        var result = employeeService.updateEmployee(1L, req);

        assertThat(result).isNotNull();
        assertThat(result.departmentName()).isNull();
    }



    @Test
    @DisplayName("Should keep manager null when manager not exists during update")
    void updateEmployee_ShouldKeepManagerNull_WhenManagerMissing() {
        var req = new EmployeeRequest();
        req.setDepartmentId(1L);
        req.setManagerId(2L);
        var dep = new Department(1L, "IT", "Tashkent");
        var existing = new Employee(1L, "Bob", "Dev", 500L, dep, null);

        lenient().when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        lenient().when(departmentRepository.findById(1L)).thenReturn(Optional.of(dep));
        lenient().when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        lenient().when(employeeRepository.save(existing)).thenReturn(existing);
        lenient().when(employeeMapper.toResponse(existing))
                .thenReturn(new EmployeeResponse(1L, "Bob", "Dev", 500L, "IT", null));

        var result = employeeService.updateEmployee(1L, req);

        assertThat(result).isNotNull();
        assertThat(result.managerName()).isNull();
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(existing);
    }


    @Test
    @DisplayName("Delete employee when exists")
    void deleteEmployee_ShouldWork_WhenExists() {
        var emp = new Employee(1L, "John", "Dev", 700L, new Department(), null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        employeeService.deleteEmployee(1L);
        verify(employeeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Throw EmployeeNotFoundException when delete target missing")
    void deleteEmployee_ShouldThrow_WhenNotFound() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(10L));
    }

    @Test
    @DisplayName("Update employee should keep same department if not changed")
    void updateEmployee_ShouldKeepDepartment_WhenNullInRequest() {
        var dep = new Department(1L, "HR", "Tashkent");
        var manager = new Employee(2L, "Boss", "Manager", 900L, dep, null);
        var existing = new Employee(1L, "Bob", "Developer", 700L, dep, manager);
        var req = new EmployeeRequest();
        req.setName("Updated");
        req.setPosition("Lead");
        req.setSalary(1000L);

        var resp = new EmployeeResponse(1L, "Updated", "Lead", 1000L, "HR", "Boss");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(existing)).thenReturn(existing);
        when(employeeMapper.toResponse(existing)).thenReturn(resp);

        var result = employeeService.updateEmployee(1L, req);
        assertThat(result).isEqualTo(resp);
    }
}
