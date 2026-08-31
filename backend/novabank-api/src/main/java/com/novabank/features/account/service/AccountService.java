package com.novabank.features.account.service;

import com.novabank.features.account.entity.Account;
import com.novabank.features.account.repository.AccountRepository;
import com.novabank.features.customer.repository.CustomerRepository;
import com.novabank.features.customer.service.CustomerService;
import com.novabank.infra.exception.BusinessException;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final CustomerService customerService;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
            CustomerRepository customerRepository, CustomerService customerService) {

        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    public Account createAccount(Account account) {

        customerService.getCustomerById(account.getCustomerId());

        if (accountRepository.findByAccountNumber(account.getAccountNumber()).isPresent()) {
            throw new BusinessException(
                    "Account number already exists: " + account.getAccountNumber());
        }

        if (account.getAccountNumber() == null || account.getAccountNumber().isEmpty()) {
            account.setAccountNumber(generateAccountNumber());
        }

        return accountRepository.save(account);
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateAccountNumber() {
        return System.currentTimeMillis() + String.format("%04d", RANDOM.nextInt(10_000));
    }

}
