package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
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
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    @Captor
    private ArgumentCaptor<Employee> employeeCaptor;

    private Employee employee;
    private EmployeeRequest employeeRequest;
    private EmployeeResponse employeeResponse;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Department 1");
        department.setLocation("Tashkent");

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setName("Alice Doe");
        manager.setPosition("Developer");
        manager.setSalary(5000L);
        manager.setDepartment(department);

        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setPosition("Developer");
        employee.setSalary(5000L);
        employee.setDepartment(department);
        employee.setManager(manager);

        employeeRequest = new EmployeeRequest();
        employeeRequest.setName("John Doe");
        employeeRequest.setPosition("Developer");
        employeeRequest.setSalary(5000L);
        employeeRequest.setDepartmentId(1L);
        employeeRequest.setManagerId(2L);

        employeeResponse = new EmployeeResponse(
                1L, "John Doe", "Developer", 5000L,
                department.getName(), "Alice Doe"
        );
    }

    @Test
    @DisplayName("getEmployeeById — возвращает сотрудника, если найден")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.getEmployeeById(1L);

        assertThat(result)
                .isNotNull()
                .isEqualTo(employeeResponse);

        verify(employeeRepository).findById(1L);
        verify(employeeMapper).toResponse(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("getEmployeeById — выбрасывает исключение, если сотрудник не найден")
    void getEmployeeById_ShouldThrowException_WhenNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(1L));

        verify(employeeRepository).findById(1L);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("createEmployee — сохраняет сотрудника и возвращает ответ")
    void createEmployee_ShouldSaveAndReturnResponse() {
        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.createEmployee(employeeRequest);

        assertThat(result)
                .isNotNull()
                .isEqualTo(employeeResponse);

        verify(employeeRepository).save(employeeCaptor.capture());
        verify(employeeMapper).toResponse(employee);
        assertThat(employeeCaptor.getValue().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("getAllEmployees — возвращает список всех сотрудников")
    void getAllEmployees_ShouldReturnListOfEmployeeResponses() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(employeeResponse);

        verify(employeeRepository).findAll();
        verify(employeeMapper).toResponse(employeeCaptor.capture());
    }

    @Test
    @DisplayName("deleteEmployee — выбрасывает исключение, если сотрудник не найден")
    void deleteEmployee_ShouldThrowException_WhenEmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(1L));

        verify(employeeRepository).findById(1L);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("updateEmployee — обновляет данные сотрудника, если найден")
    void updateEmployee_ShouldUpdateAndReturnResponse_WhenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.updateEmployee(1L, employeeRequest);

        assertThat(result)
                .isNotNull()
                .isEqualTo(employeeResponse);

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(employeeCaptor.capture());
        verify(employeeMapper).toResponse(employee);

        Employee captured = employeeCaptor.getValue();
        assertThat(captured.getName()).isEqualTo(employeeRequest.getName());
        assertThat(captured.getPosition()).isEqualTo(employeeRequest.getPosition());
        assertThat(captured.getSalary()).isEqualTo(employeeRequest.getSalary());
        assertThat(captured.getDepartment()).isEqualTo(employee.getDepartment());
        assertThat(captured.getManager()).isEqualTo(employee.getManager());
    }
}
