package com.onlineshop.test.repository;

import com.onlineshop.test.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Integration tests
@DataJpaTest
@Testcontainers
class CustomerRepositoryTest extends RepositoryBaseTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    public void setUp() {
        customer = new Customer();
        customer.setName("John");
        customer.setCity("New York");
    }

    @Test
    void testSave() {
        Customer savedCustomer = customerRepository.save(customer);
        assertNotNull(savedCustomer.getId());
    }
}