package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/employees - returns list of employees")
    void getAllEmployees() throws Exception {
        List<EmployeeResponse> list = List.of(
                new EmployeeResponse( 1L, "John", "john@mail.com", "Manager" ),
                new EmployeeResponse( 2L, "Sara", "sara@mail.com", "Admin" )
        );

        when( employeeService.getAllEmployees() ).thenReturn( list );

        String responseJson = mockMvc.perform( get( "/api/employees" ) )
                .andExpect( status().isOk() )
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeResponse[] responseArray = objectMapper.readValue( responseJson, EmployeeResponse[].class );
        assertThat( responseArray ).hasSize( 2 )
                .extracting( EmployeeResponse::getName )
                .containsExactlyInAnyOrder( "Shoh", "Jahon" );

        verify( employeeService ).getAllEmployees();
    }

    @Test
    @DisplayName("GET /api/employees/{id} - returns employee when found")
    void getEmployeeById() throws Exception {
        EmployeeResponse response = new EmployeeResponse( 1L, "Shoh", "shoh@mail.com", "Manager" );

        when( employeeService.getEmployeeById( 1L ) ).thenReturn( response );

        String json = mockMvc.perform( get( "/api/employees/1" ) )
                .andExpect( status().isOk() )
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeResponse returned = objectMapper.readValue( json, EmployeeResponse.class );

        assertThat( returned )
                .isNotNull()
                .extracting( EmployeeResponse::getId, EmployeeResponse::getName )
                .containsExactly( 1L, "Shoh" );

        verify( employeeService ).getEmployeeById( 1L );
    }

    @Test
    @DisplayName("GET /api/employees/{id} - 404 when employee not found")
    void getEmployeeNotFound() throws Exception {
        when( employeeService.getEmployeeById( 99L ) )
                .thenThrow( new RuntimeException( "Employee not found" ) );

        mockMvc.perform( get( "/api/employees/99" ) )
                .andExpect( status().isNotFound() );

        verify( employeeService ).getEmployeeById( 99L );
    }

    @Test
    @DisplayName("POST /api/employees - creates employee successfully")
    void createEmployee() throws Exception {
        EmployeeRequest request = new EmployeeRequest( "Shoh", "shoh@mail.com", "Manager" );
        EmployeeResponse response = new EmployeeResponse( 1L, "Shoh", "shoh@mail.com", "Manager" );

        when( employeeService.createEmployee( request ) ).thenReturn( response );

        String body = objectMapper.writeValueAsString( request );

        String json = mockMvc.perform( post( "/api/employees" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( body ) )
                .andExpect( status().isOk() )
                .andReturn().getResponse().getContentAsString();

        EmployeeResponse returned = objectMapper.readValue( json, EmployeeResponse.class );
        assertThat( returned.getId() ).isEqualTo( 1L );
        assertThat( returned.getEmail() ).isEqualTo( "shoh@mail.com" );

        verify( employeeService ).createEmployee( request );
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - updates employee")
    void updateEmployee() throws Exception {
        EmployeeRequest request = new EmployeeRequest( "Jahon", "jahon@mail.com", "Lead" );
        EmployeeResponse response = new EmployeeResponse( 1L, "Jahon", "jahon@mail.com", "Lead" );

        when( employeeService.updateEmployee( eq( 1L ), any() ) ).thenReturn( response );

        String body = objectMapper.writeValueAsString( request );

        String json = mockMvc.perform( put( "/api/employees/1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( body ) )
                .andExpect( status().isOk() )
                .andReturn().getResponse().getContentAsString();

        EmployeeResponse returned = objectMapper.readValue( json, EmployeeResponse.class );

        assertThat( returned.getName() ).isEqualTo( "Jahon" );
        assertThat( returned.getRole() ).isEqualTo( "Lead" );

        verify( employeeService ).updateEmployee( eq( 1L ), any() );
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - delete successfully")
    void deleteEmployee() throws Exception {
        doNothing().when( employeeService ).deleteEmployee( 1L );

        mockMvc.perform( delete( "/api/employees/1" ) )
                .andExpect( status().isOk() );

        verify( employeeService ).deleteEmployee( 1L );
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - 404 when employee not found")
    void deleteEmployeeNotFound() throws Exception {
        doThrow( new RuntimeException( "Employee not found" ) )
                .when( employeeService ).deleteEmployee( 99L );

        mockMvc.perform( delete( "/api/employees/99" ) )
                .andExpect( status().isNotFound() );

        verify( employeeService ).deleteEmployee( 99L );
    }
}
