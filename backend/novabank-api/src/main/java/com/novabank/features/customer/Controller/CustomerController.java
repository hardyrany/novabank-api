package com.novabank.features.customer.Controller;

import javax.print.DocFlavor.READER;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novabank.features.customer.dto.CustomerRequest;
import com.novabank.features.customer.dto.CustomerResponse;
import com.novabank.features.customer.entity.Customer;
import com.novabank.features.customer.mapper.CustomerMapper;
import com.novabank.features.customer.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    public CustomerController(CustomerService customerService, CustomerMapper customerMapper) {
        this.customerService = customerService;
        this.customerMapper = customerMapper;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request) {

        Customer customer = new CustomerMapper().toEntity(request);

        Customer savedCustomer = customerService.createCustomer(customer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerMapper.toResponse(savedCustomer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {

        Customer customer = customerService.getCustomerById(id);

        return ResponseEntity.ok(customerMapper.toResponse(customer));
    }

}
