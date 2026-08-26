package com.novabank.features.customer.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        public ResponseEntity<CustomerResponse> getCustomerById(
                        @PathVariable Long id) {

                Customer customer = customerService
                                .getCustomerById(id);

                return ResponseEntity.ok(
                                customerMapper.toResponse(customer));
        }

        @GetMapping("/email/{email}")
        public ResponseEntity<CustomerResponse> getCustomerByEmail(
                        @PathVariable String email) {

                Customer customer = customerService
                                .getCustomerByEmail(email);

                return ResponseEntity.ok(
                                customerMapper.toResponse(customer));
        }

        @GetMapping("/document/{documentNumber}")
        public ResponseEntity<CustomerResponse> getCustomerByDocumentNumber(
                        @PathVariable String documentNumber) {

                Customer customer = customerService
                                .getCustomerByDocumentNumber(documentNumber);

                return ResponseEntity.ok(
                                customerMapper.toResponse(customer));
        }

        @GetMapping
        public ResponseEntity<List<CustomerResponse>> getAllCustomers() {

                List<Customer> customers = customerService.getAllCustomers();
                List<CustomerResponse> responses = customers.stream()
                                .map(customerMapper::toResponse)
                                .toList();

                return ResponseEntity.ok(responses);
        }

        @GetMapping("/search")
        public ResponseEntity<List<CustomerResponse>> searchCustomersByName(
                        @RequestParam String name) {

                List<Customer> customers = customerService.searchCustomersByName(name);
                List<CustomerResponse> responses = customers.stream()
                                .map(customerMapper::toResponse)
                                .toList();

                return ResponseEntity.ok(responses);
        }

        @PutMapping("/{id}")
        public ResponseEntity<CustomerResponse> updateCustomer(
                        @PathVariable Long id, @Valid @RequestBody CustomerRequest request) {

                Customer customer = new Customer();

                customer.setFirstName(request.getFirstName());
                customer.setMiddleName(request.getMiddleName());
                customer.setLastName(request.getLastName());
                customer.setPhone(request.getPhone());
                customer.setBirthDate(request.getBirthDate());
                customer.setAddress(request.getAddress());

                Customer updatedCustomer = customerService.updateCustomer(id, customer);

                return ResponseEntity.ok(customerMapper.toResponse(updatedCustomer));
        }

}
