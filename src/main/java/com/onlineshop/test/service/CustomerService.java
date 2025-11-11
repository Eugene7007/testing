package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.CustomerRequest;
import com.onlineshop.test.dto.response.CustomerResponse;
import com.onlineshop.test.entity.Customer;
import com.onlineshop.test.exception.CustomerNotFoundException;
import com.onlineshop.test.mapper.CustomerMapper;
import com.onlineshop.test.repository.CustomerRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerService {

    CustomerMapper customerMapper;
    CustomerRepository customerRepository;

    // Получение всех клиентов
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository
                .findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    // Получение клиента по ID
    public CustomerResponse getCustomerById(Long id) {
        return customerRepository
                .findById(id)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    // Создание нового клиента
    public CustomerResponse createCustomer(CustomerRequest request) {
        var customer = customerMapper.toEntity(request);
        customerRepository.save(customer);

        return customerMapper.toResponse(customer);
    }

    // Обновление клиента
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        var existingCustomer = customerRepository
                .findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        existingCustomer.setName(request.getName());
        existingCustomer.setCity(request.getCity());
        customerRepository.save(existingCustomer);

        return customerMapper.toResponse(existingCustomer);
    }

    // Удаление клиента
    public void deleteCustomer(Long id) {
        customerRepository
                .findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customerRepository.deleteById(id);
    }
}