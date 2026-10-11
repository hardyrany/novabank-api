package com.novabank.features.customer.mapper;

import org.springframework.stereotype.Component;

import com.novabank.features.customer.dto.CustomerRequest;
import com.novabank.features.customer.dto.CustomerResponse;
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

    public CustomerResponse toResponse(Customer customer) {

        CustomerResponse response = new CustomerResponse();

        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setMiddleName(customer.getMiddleName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setDocumentNumber(customer.getDocumentNumber());
        response.setDocumentType(customer.getDocumentType());
        response.setBirthDate(customer.getBirthDate());
        response.setAddress(customer.getAddress());
        response.setActive(customer.isActive());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        return response;

    }
}
