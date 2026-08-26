package com.novabank.features.customer.mapper;

import org.springframework.stereotype.Component;

import com.novabank.features.customer.dto.CustomerRequest;
import com.novabank.features.customer.entity.Customer;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {

        Customer customer = new Customer();

        customer.setFirstName(request.getFirstName());
        customer.setMiddleName(request.getMiddleName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDocumentNumber(request.getDocumentNumber());
        customer.setDocumentType(request.getDocumentType());
        customer.setBirthDate(request.getBirthDate());
        customer.setAddress(request.getAddress());

        return customer;

    }
}
