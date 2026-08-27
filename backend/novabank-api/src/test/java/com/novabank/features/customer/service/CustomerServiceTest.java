package com.novabank.features.customer.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.novabank.features.customer.entity.Customer;
import com.novabank.features.customer.repository.CustomerRepository;
import com.novabank.features.customer.service.CustomerService;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ResourceNotFoundException;

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

        @Test
        void createCustomer_WithDuplicateEmail_ShouldThrowBusinessException() {

                // Arrange
                Customer customer = new Customer();

                customer.setEmail("john@example.com");

                when(customerRepository
                                .existsByEmail(customer.getEmail()))
                                .thenReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> customerService.createCustomer(customer))
                                .isInstanceOf(BusinessException.class)
                                .hasMessage("Email already exists: john@example.com");

                verify(customerRepository, never()).save(any());
        }

        @Test
        void createCustomer_WithDuplicateDocument_ShouldThrowBusinessException() {

                // Arrange
                Customer customer = new Customer();

                customer.setEmail("john@example.com");
                customer.setDocumentNumber("123456789");

                when(customerRepository
                                .existsByEmail(customer.getEmail()))
                                .thenReturn(false);
                when(customerRepository
                                .existsByDocumentNumber(customer.getDocumentNumber()))
                                .thenReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> customerService.createCustomer(customer))
                                .isInstanceOf(BusinessException.class)
                                .hasMessage("Document number already exists: 123456789");

                verify(customerRepository, never()).save(any());
        }

        @Test
        void getCustomerById_ShouldReturnCustomer() {

                // Arrange
                Long customerId = 1L;
                Customer customer = new Customer();

                customer.setId(customerId);

                when(customerRepository
                                .findById(customerId))
                                .thenReturn(Optional.of(customer));

                // Act
                Customer result = customerService.getCustomerById(customerId);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(customerId);
        }

        @Test
        void getCustomerById_WhenNoFound_ShouldThrowResourceNotFoundException() {

                // Arrange
                Long customerId = 999L;

                when(customerRepository
                                .findById(customerId))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> customerService
                                .getCustomerById(customerId))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Customer not found with id: 999");
        }

        @Test
        void getCustomerByEmail_ShouldReturnCustomer() {

                // Arrange
                String customerEmail = "john@example.com";
                Customer customer = new Customer();

                customer.setEmail(customerEmail);

                when(customerRepository
                                .findByEmail(customerEmail))
                                .thenReturn(Optional.of(customer));

                // Act
                Customer result = customerService
                                .getCustomerByEmail(customerEmail);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getEmail()).isEqualTo(customerEmail);

        }

        @Test
        void getCustomerByEmail_WhenNotFound_ShouldThrowResourceNotFoundException() {

                // Arrange
                String customerEmail = "notfound@example.com";

                when(customerRepository
                                .findByEmail(customerEmail))
                                .thenReturn(Optional.empty());

                // Act & Assert

                assertThatThrownBy(() -> customerService
                                .getCustomerByEmail(customerEmail))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Email not found: notfound@example.com");

        }

        @Test
        void getCustomerByDocumentNumber_ShouldReturnCustomer() {

                String documentNumber = "ABCD123EF";
                Customer customer = new Customer();

                customer.setDocumentNumber(documentNumber);

                when(customerRepository
                                .findByDocumentNumber(documentNumber))
                                .thenReturn(Optional.of(customer));

                Customer result = customerService
                                .getCustomerByDocumentNumber(documentNumber);

                assertThat(result).isNotNull();
                assertThat(result.getDocumentNumber()).isEqualTo(documentNumber);
        }

        @Test
        void getCustomerByDocumentNumber_WhenNotFound_ShouldThrowResourceNotFoundException() {

                String documentNumber = "ABCD123EF";

                when(customerRepository
                                .findByDocumentNumber(documentNumber))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> customerService
                                .getCustomerByDocumentNumber(documentNumber))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Document number not found: ABCD123EF");
        }

        @Test
        void searchCustomersByName_ShouldReturnMatchingCustomers() {

                String customerName = "john";

                List<Customer> customers = List.of(new Customer(), new Customer());

                when(customerRepository
                                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                                                customerName, customerName))
                                .thenReturn(customers);

                List<Customer> result = customerService
                                .searchCustomersByName(customerName);

                assertThat(result).hasSize(2);
        }

        @Test
        void searchCustomersByName_WhenNameIsEmpty_ShouldReturnAllCustomers() {

                List<Customer> customers = List.of(new Customer(), new Customer(), new Customer());

                when(customerRepository
                                .findAll())
                                .thenReturn(customers);

                List<Customer> result = customerService
                                .searchCustomersByName("");

                assertThat(result).hasSize(3);
                verify(customerRepository, never())
                                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                                                any(), any());

        }
}