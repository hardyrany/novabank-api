package com.novabank.infra.security;

import org.springframework.stereotype.Component;
import com.novabank.features.customer.repository.CustomerRepository;

@Component
public class OwnershipValidator {

    private final CustomerRepository customerRepository;

    public OwnershipValidator(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
}
