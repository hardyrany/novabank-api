package com.novabank.features.customer.service;

import org.springframework.stereotype.Service;

import com.novabank.features.customer.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

}
