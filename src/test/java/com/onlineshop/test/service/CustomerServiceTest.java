package com.onlineshop.test.service;


import com.onlineshop.test.dto.request.CustomerRequest;
import com.onlineshop.test.dto.response.CustomerResponse;
import com.onlineshop.test.entity.Customer;
import com.onlineshop.test.exception.CustomerNotFoundException;
import com.onlineshop.test.mapper.CustomerMapper;
import com.onlineshop.test.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;
    @InjectMocks
    private CustomerService customerService;
    @Captor
    ArgumentCaptor<Customer> customerCaptor;

    @Test
    @DisplayName("getById")
    void getCustomerById_WhenExists_ReturnsCustomer() {
        //Arrange
        Customer customer = new Customer();

        var customerId = 1L;

        var customerResponse = new CustomerResponse(1L, "Lappland", "Siracusa");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        //Act
        var response = customerService.getCustomerById(customerId);

        //Assert
        assertThat(response).isNotNull();
        assertThat(customerResponse)
            .isNotNull()
            .isEqualTo(response);

        //Verify
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerMapper, times(1)).toResponse(customer);
        verify(customerMapper, times(1)).toResponse(customerCaptor.capture());

    }

    @Test
    void getCustomerById_WhenExistsNot_ReturnsCustomer(){
        //Arrange

        var customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> customerService.getCustomerById(customerId));

        //Verify
        verify(customerRepository, times(1)).findById(customerId);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName("displayNames")
    void getAllCustomers_shouldReturnList_whenCustomersExist() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setName("Lappland");
        customer1.setCity("Siracusa");

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("Nearl The Radiant Knight");
        customer2.setCity("Kaziemrz");

        CustomerResponse response1 = new CustomerResponse(1L, "Lappland", "Siracusa");
        CustomerResponse response2 = new CustomerResponse(2L, "Nearl The Radiant Knight", "Kaziemrz");

       // Act
        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));
        when(customerMapper.toResponse(customer1)).thenReturn(response1);
        when(customerMapper.toResponse(customer2)).thenReturn(response2);

        List<CustomerResponse> result = customerService.getAllCustomers();

        // Assert
        assertThat(result)
            .hasSize(2)
            .containsExactly(response1, response2);

        // Verify
        verify(customerRepository).findAll();
        verify(customerMapper).toResponse(customer1);
        verify(customerMapper).toResponse(customer2);
    }

    @Test
    void getAllCustomers_shouldReturnNoList_whenNoCustomersExist() {
        // Arrange
        when(customerRepository.findAll()).thenReturn(List.of());

        // Act
        List<CustomerResponse> result = customerService.getAllCustomers();

        // Assert
        assertThat(result)
            .isNotNull()
            .isEmpty();

        // Verify
        verify(customerRepository).findAll();
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName("newCustomer")
    void createCustomer_shouldReturnCustomer_whenValidRequest() {
        // Arrange
        var customerId = 1L;

        CustomerRequest request = new CustomerRequest();
        request.setName("Amiya");
        request.setCity("Rim Billiton");

        Customer customer = new Customer(customerId, "Amiya", "Rim Billiton");

        Customer savedCustomer = new Customer(customerId, "Amiya", "Rim Billiton");

        CustomerResponse expectedResponse = new CustomerResponse(
            customerId, "Amiya", "Rim Billiton");

        // Act
        when(customerMapper.toEntity(request)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(savedCustomer);
        when(customerMapper.toResponse(customer)).thenReturn(expectedResponse);

        CustomerResponse result = customerService.createCustomer(request);

        // Assert
        assertThat(result)
            .isNotNull()
            .isEqualTo(expectedResponse);

        // Verify
        verify(customerMapper).toEntity(request);
        verify(customerRepository).save(customer);
    }

    @Test
    void createCustomer_shouldThrowException_whenNoCustomersExist() {
        // Arrange
        CustomerRequest request = new CustomerRequest();
        request.setName("Amiya");
        request.setCity("Rim Billiton");

        Customer customer = new Customer();

        // Act
        when(customerMapper.toEntity(request)).thenReturn(customer);
        when(customerRepository.save(customer))
            .thenThrow(new RuntimeException("Database connection failed"));

        //Assert
        assertThatThrownBy(() -> customerService.createCustomer(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database connection failed");

        //Verify
        verify(customerMapper).toEntity(request);
        verify(customerRepository).save(customer);
        verify(customerMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("updatedCustomer")
    void updateCustomer_shouldUpdateCustomer_whenIdExists() {
        // Arrange
        var customerId = 1L;
        CustomerRequest request = new CustomerRequest();
        request.setName("Texas The Siracusano");
        request.setCity("Rhodes Island");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setName("Texas");
        existingCustomer.setCity("Siracusa");

        CustomerResponse expectedResponse = new CustomerResponse(
            customerId, "Texas The Siracusano", "Rhodes Island");

        //Act
        when(customerRepository.findById(customerId))
            .thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);
        when(customerMapper.toResponse(existingCustomer)).thenReturn(expectedResponse);

        CustomerResponse result = customerService.updateCustomer(customerId, request);

        // Assert
        assertThat(result)
            .isNotNull()
            .isEqualTo(expectedResponse);

        assertThat(existingCustomer.getName()).isEqualTo("Texas The Siracusano");
        assertThat(existingCustomer.getCity()).isEqualTo("Rhodes Island");

        // Verify
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(existingCustomer);
        verify(customerMapper).toResponse(existingCustomer);
    }

    @Test
    void updateCustomer_shouldThrowException_whenIdDoesNotExist() {
        // Arrange
        var customerId = 999L;
        CustomerRequest request = new CustomerRequest();
        request.setName("Texas The Siracusano");
        request.setCity("Rhodes  Island");

        // Act
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Assert
        assertThatThrownBy(() -> customerService.updateCustomer(customerId, request))
            .isInstanceOf(CustomerNotFoundException.class)
            .hasMessageContaining(String.valueOf(customerId));

        // Verify
        verify(customerRepository).findById(customerId);
        verify(customerRepository, never()).save(any());
        verifyNoInteractions(customerMapper);
    }

    @Test
    void updateCustomer_shouldThrowException_whenSaveFails() {
        // Arrange
        var customerId = 1L;
        CustomerRequest request = new CustomerRequest();
        request.setName("Texas The Siracusano");
        request.setCity("Rhodes Island");

        Customer existingCustomer = new Customer(customerId, "Texas", "Siracusa");

        // Act
        when(customerRepository.findById(customerId))
            .thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer))
            .thenThrow(new RuntimeException("ERROR: Database failed"));

        // Assert
        assertThatThrownBy(() -> customerService.updateCustomer(customerId, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("ERROR: Database failed");

        // Verify
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(existingCustomer);
        verify(customerMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("deleteCustomer")
    void deleteCustomer_shouldDelete_whenIdExists() {
        // Arrange
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);

        // Act + Assert
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        doNothing().when(customerRepository).deleteById(customerId);

        customerService.deleteCustomer(customerId);

        // Verify
        verify(customerRepository).findById(customerId);
        verify(customerRepository).deleteById(customerId);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void deleteCustomer_shouldThrowException_whenIdDoesNotExist() {
        // Arrange
        Long customerId = 999L;

        // Act
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Assert
        assertThatThrownBy(() -> customerService.deleteCustomer(customerId))
            .isInstanceOf(CustomerNotFoundException.class)
            .hasMessageContaining(customerId.toString());

        // Verify
        verify(customerRepository).findById(customerId);
        verify(customerRepository, never()).deleteById(anyLong());
    }
}
