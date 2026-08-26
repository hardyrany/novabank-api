package com.novabank.features.customer.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.novabank.features.customer.entity.Customer;
import com.novabank.features.customer.repository.CustomerRepository;
import com.novabank.features.customer.service.CustomerService;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_ShouldSaveAndReturnCustomer() {

        // Arrange
        Customer customer = new Customer();

        customer.setEmail("john@example.com");
        customer.setDocumentNumber("123456789");

        when(customerRepository
                .existsByEmail(customer.getEmail()))
                .thenReturn(false);
        when(customerRepository
                .existsByDocumentNumber(customer.getDocumentNumber()))
                .thenReturn(false);
        when(customerRepository
                .save(any(Customer.class)))
                .thenReturn(customer);

        // Act
        Customer result = customerService.createCustomer(customer);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(customerRepository).save(customer);
    }
}
