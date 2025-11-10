package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;


import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeRequest employeeRequest;
    private EmployeeResponse employeeResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
       employee = new Employee();
       employee.setId(1L);
       employee.setName("Daniel");
       employee.setPosition("Software Engineer");
       employee.setSalary(5000L);

       employeeRequest = new EmployeeRequest();
       employeeRequest.setName("Daniel");
       employeeRequest.setPosition("Software Engineer");
       employeeRequest.setSalary(5000L);

       employeeResponse = new EmployeeResponse(1L,"Daniel","Software Engineer",5000L,"IT","Mike");


    }
    @Test
    //получение всех employees
    public void getAllEmployees_returnsMappedList() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var res =  employeeService.getAllEmployees();

        assertEquals(1, res.size()); // // Проверяем. что вернулся один элемент
        assertEquals("Daniel",res.get(0).name()); // checking name
        verify(employeeRepository).findAll();  // Убеждаемся, что метод вызывался

    }

    @Test
    //получение работника по id
    public void getEmployeeById_returnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var res =  employeeService.getEmployeeById(1L);
        assertEquals("Daniel", res.name()); // Проверяем, что имя совпадает
    }

    @Test
   // если работник не найден, throws exception
    public void getEmployeeById_returnsEmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(1L));
 }

 @Test
    //creating new employee
    public void createEmployee_returnsEmployee() {
        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var res =  employeeService.createEmployee(employeeRequest);
        verify(employeeRepository).save(employee);  //Проверяем, что был вызван save()
        assertEquals("Daniel", res.name());
 }

 @Test
    //update existing employee
    public void updateEmployee_returnsResponse() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        var res = employeeService.updateEmployee(1L, employeeRequest);
        verify(employeeRepository).save(employee);
        assertEquals("Daniel", res.name());

 }
 @Test
    //обновление несуществующего employee
    public void updateEmployee_notFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
   assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(1L, employeeRequest)); }

    @Test
    //удаление несуществующего employee
    public void deleteEmployee_notFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(1L));

    }

    @Test
    //удаление существующего employee
    public void deleteEmployee_returnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.deleteEmployee(1L);
        verify(employeeRepository).deleteById(1L); //Проверяем что метод удаления вызвался
    }


    //getAllEmployees возвращает пустой список, если нет данных
    @Test
    public void getAllEmployees_returnsEmptyList() {
        when(employeeRepository.findAll()).thenReturn(List.of());
        var res =  employeeService.getAllEmployees();
        assertEquals(0, res.size());

    }

    @Test
    //проверка, что deleteEmployee вызывает методы ровно один раз
    public void deleteEmployee_verifyMethodCallCount(){
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.deleteEmployee(1L);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    // проверка, что createEmployee вызывает mapper и репозиторий один раз
    @Test
    public void createEmployee_callMapperAndRepositoryOnce(){
        when(employeeMapper.toEntity(employeeRequest)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        employeeService.createEmployee(employeeRequest);
        verify(employeeMapper, times(1)).toEntity(employeeRequest);
        verify(employeeMapper, times(1)).toResponse(employee);
        verify(employeeRepository, times(1)).save(employee);

    }

    @Test
    // проверка, что updateEmployee обновляет поля
    public void updateEmployee_correctFieldChanges(){
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

        employeeRequest.setName("Anne");
        employeeRequest.setPosition("Manager");
        employeeRequest.setSalary(6000L);

        employeeService.updateEmployee(1L, employeeRequest);
        assertEquals("Anne", employeeRequest.getName());
        assertEquals("Manager", employeeRequest.getPosition());
        assertEquals(6000L, employee.getSalary().longValue());
        verify(employeeRepository).save(employee);
  }


}
