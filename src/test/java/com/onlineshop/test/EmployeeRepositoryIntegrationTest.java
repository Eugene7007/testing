package repository;

import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryIntegrationTest {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    EntityManager entityManager;

    private Department department;
    private Employee manager;

    @BeforeEach
    void setup() {
        department = new Department();
        department.setName( "IT" );
        entityManager.persist( department );

        manager = new Employee();
        manager.setName( "Shoh Manager" );
        manager.setPosition( "Manager" );
        manager.setSalary( 100000L );
        manager.setDepartment( department );

        entityManager.persist( manager );
        entityManager.flush();
    }

    @Test
    @DisplayName("Save employee successfully")
    void saveEmployee() {
        Employee employee = new Employee( null, "Shoh Jahon", "Developer", 50000L, department, manager );
        Employee saved = employeeRepository.save( employee );

        assertThat( saved.getId() ).isNotNull();
        assertThat( saved.getName() ).isEqualTo( "Shoh Jahon" );
    }

    @Test
    @DisplayName("Find employee by ID")
    void findById() {
        Employee employee = new Employee( null, "Shoh", "Developer", 50000L, department, manager );
        Employee saved = employeeRepository.save( employee );

        Optional<Employee> found = employeeRepository.findById( saved.getId() );

        assertThat( found )
                .isPresent()
                .get()
                .extracting( Employee::getName )
                .isEqualTo( "Shoh" );
    }

    @Test
    @DisplayName("Find all employees")
    void findAllEmployees() {
        employeeRepository.save( new Employee( null, "Shoh", "Developer", 50000L, department, manager ) );
        employeeRepository.save( new Employee( null, "Jahon", "Tester", 40000L, department, manager ) );

        List<Employee> list = employeeRepository.findAll();

        assertThat( list ).hasSizeGreaterThanOrEqualTo( 3 );
    }

    @Test
    @DisplayName("Update employee salary")
    void updateEmployee() {
        Employee saved = employeeRepository.save(
                new Employee( null, "Shoh", "Developer", 50000L, department, manager )
        );

        saved.setSalary( 55000L );
        Employee updated = employeeRepository.save( saved );

        assertThat( updated.getSalary() ).isEqualTo( 55000L );
    }

    @Test
    @DisplayName("Delete employee")
    void deleteEmployee() {
        Employee saved = employeeRepository.save(
                new Employee( null, "Shoh", "Developer", 50000L, department, manager )
        );

        employeeRepository.delete( saved );

        assertThat( employeeRepository.findById( saved.getId() ) ).isEmpty();
    }

    @Test
    @DisplayName("Find employees by manager")
    void findByManager() {
        Employee e1 = employeeRepository.save( new Employee( null, "Shoh", "Developer", 50000L, department, manager ) );
        Employee e2 = employeeRepository.save( new Employee( null, "Jahon", "Tester", 40000L, department, manager ) );

        List<Employee> underManager = employeeRepository.findAll().stream()
                .filter( emp -> manager.equals( emp.getManager() ) )
                .toList();

        assertThat( underManager ).containsExactlyInAnyOrder( e1, e2 );
    }

    @Test
    @DisplayName("Find employees by department")
    void findByDepartment() {
        Employee e1 = employeeRepository.save( new Employee( null, "Shoh", "Developer", 50000L, department, manager ) );

        List<Employee> inDept = employeeRepository.findAll().stream()
                .filter( emp -> department.equals( emp.getDepartment() ) )
                .toList();

        assertThat( inDept ).contains( e1 );
    }

    @Test
    @DisplayName("Find employee by non-existent ID returns empty")
    void findByNonExistentId() {
        assertThat( employeeRepository.findById( 999L ) ).isEmpty();
    }

    @Test
    @DisplayName("Save employee with null manager")
    void saveEmployeeNullManager() {
        Employee saved = employeeRepository.save(
                new Employee( null, "No Manager", "Intern", 20000L, department, null )
        );

        assertThat( saved.getManager() ).isNull();
    }

    @Test
    @DisplayName("Save employee with null department")
    void saveEmployeeNullDepartment() {
        Employee saved = employeeRepository.save(
                new Employee( null, "No Dept", "Intern", 20000L, null, manager )
        );

        assertThat( saved.getDepartment() ).isNull();
    }

    @Test
    @DisplayName("Save multiple employees under same manager")
    void multipleEmployeesSameManager() {
        Employee e1 = employeeRepository.save( new Employee( null, "Shoh", "Developer", 50000L, department, manager ) );
        Employee e2 = employeeRepository.save( new Employee( null, "Jahon", "Tester", 40000L, department, manager ) );

        List<Employee> underManager = employeeRepository.findAll().stream()
                .filter( emp -> manager.equals( emp.getManager() ) )
                .toList();

        assertThat( underManager ).contains( e1, e2 );
    }

    @Test
    @DisplayName("Should save and find employee")
    void shouldSaveAndFindEmployee() {
        Employee saved = employeeRepository.save(
                new Employee( null, "Jahon", "Manager", 9000L, null, null )
        );

        Optional<Employee> found = employeeRepository.findById( saved.getId() );

        assertThat( found )
                .isPresent()
                .flatMap( Optional::stream )
                .extracting( Employee::getName )
                .isEqualTo( "Jahon" );

        assertThat( saved.getSalary() ).isGreaterThan( 8000L );
    }
}
