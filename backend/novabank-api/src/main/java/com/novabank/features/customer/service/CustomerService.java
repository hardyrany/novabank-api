package com.novabank.features.customer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novabank.features.customer.entity.Customer;
import com.novabank.features.customer.repository.CustomerRepository;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ResourceNotFoundException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new BusinessException("Email already exists: " + customer.getEmail());
        }

        if (customer.getDocumentNumber() != null &&
                customerRepository.existsByDocumentNumber(customer.getDocumentNumber())) {
            throw new BusinessException("Document number already exists: "
                    + customer.getDocumentNumber());
        }

        return customerRepository.save(customer);
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + id));
    }

}
