package com.onlineshop.test.controller;


import com.onlineshop.test.controller.EmployeeController;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    EmployeeService employeeService;

    @InjectMocks
    EmployeeController employeeController;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    //    1 получение всех сотрудников
    @Test
    @DisplayName("Получение всех сотрудников")
    void getMapping_ShouldReturnListOfAllEMployees() {
        List<EmployeeResponse> responses = Arrays.asList(
                new EmployeeResponse(1L, "Lance", "Manager", 5000L, null, null),
                new EmployeeResponse(2L, "Max", "Developer", 10000L, null, null)
        );

        when(employeeService.getAllEmployees()).thenReturn(responses);

        List<EmployeeResponse> result = employeeController.getAllEmployees();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Lance");
        assertThat(result.get(1).name()).isEqualTo("Max");

        verify(employeeService, times(1)).getAllEmployees();
    }


    // 2 получение пустого списка сотрудников
    @Test
    @DisplayName("Получение пустого списка сотрудников")
    void getMapping_ShouldReturnEmptyList() {

        when(employeeService.getAllEmployees()).thenReturn(List.of());

        List<EmployeeResponse> result = employeeController.getAllEmployees();

        assertThat(result).isEmpty();
        verify(employeeService, times(1)).getAllEmployees();
    }

//    3 get by ID, если тот найден
@Test
@DisplayName("Получение сотрудника по ID — найден")
void getEmployeeById_ShouldReturnEmployee() {
    Long id = 1L;
    EmployeeResponse response = new EmployeeResponse(id, "Lance", "Manager", 4000L, null, null);
    when(employeeService.getEmployeeById(id)).thenReturn(response);

    EmployeeResponse result = employeeController.getEmployeeById(id);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(id);
    assertThat(result.name()).isEqualTo("Lance");

    verify(employeeService, times(1)).getEmployeeById(id);
}

// 4 на случай если сотрудник не найден
@Test
@DisplayName("Получение сотрудника по ID — не найден")
void getEmployeeById_ShouldThrowException_WhenNotFound() {
    Long id = 99L;
    when(employeeService.getEmployeeById(id))
            .thenThrow(new RuntimeException("Employee not found"));

    org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> employeeController.getEmployeeById(id),
            "Employee not found"
    );

    verify(employeeService, times(1)).getEmployeeById(id);
}

// 5 успешное создание сотрудника
@Test
@DisplayName("Создание нового сотрудника — успех")
void createEmployee_ShouldReturnCreatedEmployee() {
    EmployeeRequest request = new EmployeeRequest();

    EmployeeResponse response = new EmployeeResponse(1L, "Lance", "Manager", 5000L, null, null);

    when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(response);

    EmployeeResponse result = employeeController.createEmployee(request);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Lance");

    verify(employeeService, times(1)).createEmployee(any(EmployeeRequest.class));
}

// 6 fail test на предыдущий
@Test
@DisplayName("Создание нового сотрудника — преднамеренно FAIL")
void createEmployee_ShouldFailTest() {
    EmployeeRequest request = new EmployeeRequest();
    EmployeeResponse response = new EmployeeResponse(1L, "Lance", "Manager", 5000L, null, null);

    // сервис вернёт реальный объект, независимо от запроса
    when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(response);

    EmployeeResponse result = employeeController.createEmployee(request);

    // then — проверяем неправильное имя, тест должен упасть
    assertThat(result.name()).isEqualTo("Max");

    verify(employeeService, times(1)).createEmployee(any(EmployeeRequest.class));
}

// 7 успешное обновление сущности сотрудника
@Test
@DisplayName("успешное обновление сотрудника")
void updateEmployee_ShouldReturnUpdatedEmployee() {
    Long id = 1L;
    EmployeeRequest request = new EmployeeRequest();
    EmployeeResponse response = new EmployeeResponse(id, "Lance", "Manager", 6000L, null, null);

    when(employeeService.updateEmployee(eq(id), any(EmployeeRequest.class))).thenReturn(response);

    EmployeeResponse result = employeeController.updateEmployee(id, request);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(id);
    assertThat(result.name()).isEqualTo("Lance");
    assertThat(result.salary()).isEqualTo(6000L);

    verify(employeeService, times(1)).updateEmployee(eq(id), any(EmployeeRequest.class));
}

// 8 сотрудник не найден
@Test
@DisplayName("Обновление сотрудника — не найден: его инопланетяне забрали")
void updateEmployee_ShouldThrowException_WhenNotFound() {
    Long id = 99L;
    EmployeeRequest request = new EmployeeRequest();

    when(employeeService.updateEmployee(eq(id), any(EmployeeRequest.class)))
            .thenThrow(new RuntimeException("Employee not found"));

    org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> employeeController.updateEmployee(id, request),
            "Employee not found"
    );

    verify(employeeService, times(1)).updateEmployee(eq(id), any(EmployeeRequest.class));
}

// 9 спец-наз
@Test
@DisplayName("Обновление сотрудника — преднамеренно FAIL")
void updateEmployee_ShouldFailTest() {
    Long id = 1L;
    EmployeeRequest request = new EmployeeRequest();
    EmployeeResponse response = new EmployeeResponse(id, "Lance", "Manager", 6000L, null, null);

    when(employeeService.updateEmployee(eq(id), any(EmployeeRequest.class))).thenReturn(response);

    EmployeeResponse result = employeeController.updateEmployee(id, request);

    // проверка неправильного имени, тест должен пасть, как Варшава в 1939-м
    assertThat(result.name()).isEqualTo("Max");

    verify(employeeService, times(1)).updateEmployee(eq(id), any(EmployeeRequest.class));
}

// 10 успешное удаление сотрудника
@Test
@DisplayName("Bluetooth device disconnected successfully")
void deleteEmployee_ShouldCallService() {
    Long id = 1L;
    doNothing().when(employeeService).deleteEmployee(id);

    employeeController.deleteEmployee(id);

    verify(employeeService, times(1)).deleteEmployee(id);
}

// 11 исключение
@Test
@DisplayName("Удаление сотрудника — не найден")
void deleteEmployee_ShouldThrowException_WhenNotFound() {
    Long id = 99L;
    doThrow(new RuntimeException("Employee not found")).when(employeeService).deleteEmployee(id);

    org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> employeeController.deleteEmployee(id),
            "Employee not found"
    );

    verify(employeeService, times(1)).deleteEmployee(id);
}









}