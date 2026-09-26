package com.novabank.infra.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.novabank.features.account.entity.Account;
import com.novabank.features.customer.entity.Customer;
import com.novabank.features.customer.repository.CustomerRepository;
import com.novabank.infra.exception.ForbiddenException;

@Component
public class OwnershipValidator {

    private final CustomerRepository customerRepository;

    public OwnershipValidator(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void validateAccountOwnership(Account account) {
        if (hasPrivilegedRole()) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new ForbiddenException("No authenticated user found");
        }

        String email = authentication.getName();

        Customer customer = customerRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new ForbiddenException("No customer associated with authenticated user"));

        if (!account.getCustomerId().equals(customer.getId())) {
            throw new ForbiddenException("Account " + account.getId()
                    + " does not belong to the authenticated customer");
        }
    }

    public void validateCustomerOwnership(Long customerId) {
        if (hasPrivilegedRole()) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new ForbiddenException("No authenticated user found");
        }

        String email = authentication.getName();

        Customer customer = customerRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new ForbiddenException("No customer associated with authenticated user"));

        if (!customerId.equals(customer.getId())) {
            throw new ForbiddenException(
                    "Customer " + customerId + " does not match the authenticated customer");
        }
    }

    private boolean hasPrivilegedRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream().anyMatch(authority -> {
            String role = authority.getAuthority();
            return "ROLE_ADMIN".equals(role) || "ROLE_SUPPORT".equals(role);
        });
    }
}
