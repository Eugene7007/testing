
import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.model.Employee;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    private EmployeeMapper mapper;
    private EmployeeRepository repository;
    private EmployeeService service;

    @BeforeEach
    void setup() {
        mapper = mock( EmployeeMapper.class );
        repository = mock( EmployeeRepository.class );
        service = new EmployeeService( mapper, repository );
    }


    @Test
    void getAllEmployees_ShouldReturnMappedResponses() {
        var emp = new Employee();
        var response = new EmployeeResponse();

        when( repository.findAll() ).thenReturn( List.of( emp ) );
        when( mapper.toResponse( emp ) ).thenReturn( response );

        var result = service.getAllEmployees();

        assertEquals( 1, result.size() );
        assertSame( response, result.get( 0 ) );
        verify( repository ).findAll();
        verify( mapper ).toResponse( emp );
    }

    @Test
    void getEmployeeById_ShouldReturnResponse_WhenExists() {
        var emp = new Employee();
        var response = new EmployeeResponse();

        when( repository.findById( 1L ) ).thenReturn( Optional.of( emp ) );
        when( mapper.toResponse( emp ) ).thenReturn( response );

        var result = service.getEmployeeById( 1L );

        assertSame( response, result );
    }

    @Test
    void getEmployeeById_ShouldThrow_WhenNotFound() {
        when( repository.findById( 2L ) ).thenReturn( Optional.empty() );

        assertThrows( EmployeeNotFoundException.class,
                () -> service.getEmployeeById( 2L ) );
    }

    @Test
    void createEmployee_ShouldMapAndSaveEntity() {
        var request = new EmployeeRequest();
        var emp = new Employee();
        var response = new EmployeeResponse();

        when( mapper.toEntity( request ) ).thenReturn( emp );
        when( mapper.toResponse( emp ) ).thenReturn( response );

        var result = service.createEmployee( request );

        verify( repository ).save( emp );
        assertSame( response, result );
    }

    @Test
    void updateEmployee_ShouldUpdateExistingEmployee() {
        var request = new EmployeeRequest();
        request.setName( "John" );
        request.setPosition( "Developer" );
        request.setSalary( 4000.0 );

        var existing = new Employee();
        existing.setName( "Old" );
        existing.setPosition( "Tester" );
        existing.setSalary( 3000.0 );

        var response = new EmployeeResponse();

        when( repository.findById( 5L ) ).thenReturn( Optional.of( existing ) );
        when( mapper.toResponse( existing ) ).thenReturn( response );

        var result = service.updateEmployee( 5L, request );

        assertEquals( "Shoh", existing.getName() );
        assertEquals( "Developer", existing.getPosition() );
        assertEquals( 4000.0, existing.getSalary() );

        verify( repository ).save( existing );
        assertSame( response, result );
    }

    @Test
    void updateEmployee_ShouldThrow_WhenNotFound() {
        var request = new EmployeeRequest();
        when( repository.findById( 7L ) ).thenReturn( Optional.empty() );

        assertThrows( EmployeeNotFoundException.class,
                () -> service.updateEmployee( 7L, request ) );
    }

    @Test
    void deleteEmployee_ShouldDelete_WhenFound() {
        var emp = new Employee();
        when( repository.findById( 10L ) ).thenReturn( Optional.of( emp ) );

        service.deleteEmployee( 10L );

        verify( repository ).deleteById( 10L );
    }

    @Test
    void deleteEmployee_ShouldThrow_WhenNotFound() {
        when( repository.findById( 99L ) ).thenReturn( Optional.empty() );

        assertThrows( EmployeeNotFoundException.class,
                () -> service.deleteEmployee( 99L ) );
        verify( repository, never() ).deleteById( any() );
    }

    @Test
    void getAllEmployees_ShouldReturnEmptyList_WhenNone() {
        when( repository.findAll() ).thenReturn( List.of() );

        var result = service.getAllEmployees();

        assertTrue( result.isEmpty() );
    }

    @Test
    void createEmployee_ShouldPassCorrectEmployeeToSave() {
        var request = new EmployeeRequest();
        request.setName( "Shoh" );

        var emp = new Employee();
        emp.setName( "Shoh" );

        when( mapper.toEntity( request ) ).thenReturn( emp );
        when( mapper.toResponse( emp ) ).thenReturn( new EmployeeResponse() );

        service.createEmployee( request );

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass( Employee.class );
        verify( repository ).save( captor.capture() );
        assertEquals( "Shoh", captor.getValue().getName() );
    }

    @Test
    void updateEmployee_ShouldNotChangeDepartmentAndManager() {
        var request = new EmployeeRequest();
        request.setName( "NewName" );
        request.setPosition( "NewPos" );
        request.setSalary( 5000.0 );

        var originalDept = new Object();
        var originalManager = new Object();

        var emp = new Employee();
        emp.setDepartment( originalDept );
        emp.setManager( originalManager );

        when( repository.findById( 1L ) ).thenReturn( Optional.of( emp ) );
        when( mapper.toResponse( emp ) ).thenReturn( new EmployeeResponse() );

        service.updateEmployee( 1L, request );

        assertSame( originalDept, emp.getDepartment() );
        assertSame( originalManager, emp.getManager() );
    }
}
