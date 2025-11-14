package com.onlineshop.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService; // Mock the service

    @Test
    //если мы отправляем корректные данные для нового сотрудника, то контроллер успешно создаёт сотрудника и возвращает json
    void createEmployee() throws Exception {


        EmployeeRequest request = new EmployeeRequest();
        request.setName("Alex");
        request.setPosition("Developer");
        request.setSalary(5000L);
        request.setDepartmentId(1L);
        request.setManagerId(null);


        EmployeeResponse response = new EmployeeResponse(
                1L, "Alex", "Developer", 5000L, "IT", null
        );


        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(response);


        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alex"))
                .andExpect(jsonPath("$.position").value("Developer"))
                .andExpect(jsonPath("$.salary").value(5000L))
                .andExpect(jsonPath("$.departmentName").value("IT"))
                .andExpect(jsonPath("$.managerName").doesNotExist());
    }
    //Пытаемся создать сотрудника с отрицательной зарплатой.
    //+ pроверяем, что контроллер возвращает ошибку 400
@Test
    public void createEmployee_withNegativeSalary() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Alex");
        request.setPosition("Developer");
        request.setSalary(-5000L);
        request.setDepartmentId(1L);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
//создать сотрудника без корректного department id
    @Test
    public void createEmployee_withNegativeDepartmentId() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Alex");
        request.setPosition("Developer");
        request.setDepartmentId(null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createEmployee_withNoName() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setPosition("Developer");
        request.setSalary(5000L);
        request.setDepartmentId(1l);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
                }

    @Test
    public void createEmployee_withNoSalary() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Alex");
        request.setPosition("Developer");
        request.setDepartmentId(1L);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
                }

//Пытаемся создать сотрудника с зарплатой 0
    @Test
    public void createEmployee_withZeroSalary() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Alex");
        request.setPosition("Developer");
        request.setSalary(0L);
        request.setDepartmentId(1L);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    //сотрудник без department id.
    @Test
    public void createEmployee_withNoDepartmentId() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Alex");
        request.setPosition("Developer");
        request.setSalary(5000L);

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    public void getAll() throws Exception {
        List<EmployeeResponse> employees = List.of(
                new EmployeeResponse(1L,"Alex","Developer",1000L, "IT", null),
                new EmployeeResponse(2L,"Bob", "Tester",3000L, "QA", "Manager Anne"));

                when(employeeService.getAllEmployees()).thenReturn(employees);

                mockMvc.perform(get("/api/employees")).andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].name").value("Alex"))
                        .andExpect(jsonPath("$[1].name").value("Bob"));
    }
    //Пытаемся получить сотрудника по несуществующему ID.
    @Test
    void getById_notFound() throws Exception {

        when(employeeService.getEmployeeById(99L))
                .thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound());
    }
//Обновляем данные employee
    @Test
    public void update() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Alex");
        request.setPosition("Developer");
        request.setSalary(5000L);
        request.setDepartmentId(1L);

        EmployeeResponse response = new EmployeeResponse(1L, "Alex", "Developer", 5000L, "IT", null );

        when(employeeService.updateEmployee(anyLong(), any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("Developer"));}

    @Test
    public void update_notFound() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Alex");
        request.setPosition("Developer");
        request.setSalary(5000L);
        request.setDepartmentId(1L);

        when(employeeService.updateEmployee(eq(100L), any()))
                .thenThrow(new EmployeeNotFoundException(100L));

    mockMvc.perform(put("/api/employees/100")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
//запрос на удаление сотрудника
    @Test
    void deleteEmployee() throws Exception {

        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk());
    }
//Пытаемся обновить сотрудника с некорректными данными
    @Test
    void updateEmployee_validationError() throws Exception {

        EmployeeRequest request = new EmployeeRequest();
        request.setName("");
        request.setPosition("Dev");
        request.setSalary(1000L);
        request.setDepartmentId(1L);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }
}
