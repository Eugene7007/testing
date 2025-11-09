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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    @Captor
    ArgumentCaptor<Employee> employeeCaptor;

    @Test
    @DisplayName("getAllEmployees")
    void getAllEmployees_shouldReturnListOfResponses() {
        //Arrange
        Department department = new Department();
        department.setId(100L);
        department.setName("Workers");

        var manager = new Employee(1L, "Alice", "Manager", 150000L, department, null);
        var employee1 = new Employee(2L, "Bob", "Builder", 100000L, department, manager);
        var employee2 = new Employee(3L, "Max", "Painter", 90000L, department, manager);

        var EmployeeResponse1 = new EmployeeResponse(2L, "Bob", "Builder", 100000L, "Workrers", "Alice");
        var EmployeeResponse2 = new EmployeeResponse(3L, "Max", "Painter", 90000L, "Workrers", "Alice");

        when(employeeRepository.findAll())
            .thenReturn(List.of(employee1,employee2));

        when(employeeMapper.toResponse(employee1))
            .thenReturn(EmployeeResponse1);
        when(employeeMapper.toResponse(employee2))
            .thenReturn(EmployeeResponse2);

        //Act
        var result = employeeService.getAllEmployees();

        //Assert
        assertEquals(2, result.size());
        assertEquals("Bob", result.get(0).name());
        assertEquals("Max", result.get(1).name());

        // Verify
        verify(employeeRepository, times(1)).findAll();
        verify(employeeMapper, times(1)).toResponse(employee1);
        verify(employeeMapper, times(1)).toResponse(employee2);
    }

    @Test
    void getAllEmployees_shouldReturnEmptyList_whenNoEmployees() {
        //Arrange
        when(employeeRepository.findAll()).thenReturn(List.of());

        //Act
        var result = employeeService.getAllEmployees();

        //Assert
        assertThat(result).isEmpty();

        //Verify
        verify(employeeRepository, times(1)).findAll();
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("getEmployeeById")
    void getEmployeeById_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        //Arrange
        var employeeId = 1L;
        var employeeEntity = new Employee();
        var employeeResponse = new EmployeeResponse(1L,"Bob", "Builder", 100000L, "Workrers", "Alice" );

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(employeeEntity));

        when(employeeMapper.toResponse(employeeEntity))
            .thenReturn(employeeResponse);

        //Act
        var result = employeeService.getEmployeeById(employeeId);

        //Assert
        assertThat(result).isNotNull();
        assertThat(employeeResponse)
            .isNotNull()
            .isEqualTo(result);

        //Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
        verify(employeeMapper, times(1)).toResponse(employeeCaptor.capture());

    }

    @Test
    void getEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        //Arrange
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.empty());

        //Act + Assert
       assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));

       //Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("createEmployee")
    void createEmployee_ShouldSaveEmployee_ReturnEmployeeResponse() {
        //Arrange
        Department department = new Department();
        department.setId(100L);
        department.setName("Workers");

        var manager = new Employee(1L, "Alice", "Manager", 150000L, department, null);

        var request = new EmployeeRequest();
        request.setName("Mary");
        request.setPosition("Manager");
        request.setSalary(150000L);
        request.setDepartmentId(department.getId());
        request.setManagerId(1L);

        var employeeEntity = new Employee(null, "Mary", "Manager", 150000L, department, null);
        var employeeResponse = new EmployeeResponse(2L,"Mary", "Manager", 150000L, "Workers", "Alice");

        when(employeeMapper.toEntity(request))
            .thenReturn(employeeEntity);
        when(employeeMapper.toResponse(employeeEntity))
        .thenReturn(employeeResponse);

        //Act
        var result = employeeService.createEmployee(request);

        //Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employeeResponse);

        //Verify
        verify(employeeMapper, times(1)).toEntity(request);
        verify(employeeMapper, times(1)).toResponse(employeeEntity);
        verify(employeeRepository, times(1)).save(employeeEntity);
    }

    @Test
    @DisplayName("updateEmployee")
    void updateEmployee_ShouldReturnEmployeeResponse_WhenEmployeeExists() {
        //Arrange
        Department department = new Department();
        department.setId(100L);
        department.setName("Workers");

        var manager = new Employee(1L, "Alice", "Manager", 150000L, department, null);
        var existingEmployee = new Employee(3L, "Max", "Painter", 90000L, department, manager);

        var request = new EmployeeRequest();
        request.setName("Mary");
        request.setPosition("Manager");
        request.setSalary(150000L);
        request.setDepartmentId(department.getId());
        request.setManagerId(1L);

        var updatedEmployee = new EmployeeResponse(3L,"Mary", "HR", 100000L, "Workers", "Alice");

        when(employeeRepository.findById(3L))
            .thenReturn(Optional.of(existingEmployee));
        when(employeeMapper.toResponse(existingEmployee))
            .thenReturn(updatedEmployee);

        //Act
        var result = employeeService.updateEmployee(3L, request);

        //Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedEmployee);

        //Verify
        verify(employeeRepository, times(1)).findById(3L);
        verify(employeeMapper, times(1)).toResponse(existingEmployee);
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    void updateEmployee_ShouldThrow_EmployeeNotFoundException_WhenEmployeeDoesNotExist(){
        //Arrange
        var employeeId = 1L;
        var request = new EmployeeRequest();
        request.setName("Lily");

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.empty());

        //Act + Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(1L, request));

        //Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    @DisplayName("deleteEmployeeById")
    void deleteEmployeeById_ShouldDeleteEmployeeResponse_WhenEmployeeExists() {
        //Arrange
        var employeeId = 3L;
        Department department = new Department();
        department.setId(100L);
        department.setName("Workers");
        var manager = new Employee(1L, "Alice", "Manager", 150000L, department, null);
        var employee = new Employee(3L, "Max", "Painter", 90000L, department, manager);

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(employee));

       //Act
        assertDoesNotThrow(() -> employeeService.deleteEmployee(employeeId));

       //Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    void deleteEmployeeById_ShouldThrowEmployeeNotFoundException_WhenEmployeeDoesNotExist() {
        //Arrange
        var employeeId = 1L;

        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.empty());

        //Act + Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));

        //Verify
        verify(employeeRepository, times(1)).findById(employeeId);
        verifyNoInteractions(employeeMapper);
    }
    
    @Test
    void ToResponse_ShouldReturnDefaultManagerName_WhenManagerIDIsNull() {
        //Arrange
        Department department = new Department();
        department.setId(100L);
        department.setName("Workers");
        var mapper = Mappers.getMapper(EmployeeMapper.class);
        var manager = new Employee(1L, "Alice", "Manager", 150000L, department, null);

        //Act
        var result = mapper.toResponse(manager);

        //Assert
        assertThat(result).isNotNull();
        assertThat(result.managerName()).isEqualTo("Нет менеджера");
    }
}



