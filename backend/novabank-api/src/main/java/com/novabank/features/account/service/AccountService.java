package com.novabank.features.account.service;

import com.novabank.features.account.entity.Account;
import com.novabank.features.account.repository.AccountRepository;
import com.novabank.features.customer.repository.CustomerRepository;
import com.novabank.features.customer.service.CustomerService;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ResourceNotFoundException;
import java.security.SecureRandom;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final CustomerService customerService;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private static final SecureRandom RANDOM = new SecureRandom();

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

    @Transactional(readOnly = true)
    public Account getAccountById(Long id) {

        return accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Account getAccountByAccountNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with number: " + accountNumber));
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByCustomerId(Long customerId) {
        customerService.getCustomerById(customerId);

        return accountRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Account> getActiveAccounts() {

        return accountRepository.findByActive(true);
    }

    public Account updateAccount(Long id, Account accountDetails) {

        Account existingAccount = getAccountById(id);

        if (accountDetails.getAccountType() != null) {
            existingAccount.setAccountType(accountDetails.getAccountType());
        }
        if (accountDetails.getCurrency() != null) {
            existingAccount.setCurrency(accountDetails.getCurrency());
        }

        return accountRepository.save(existingAccount);
    }

    private String generateAccountNumber() {
        return System.currentTimeMillis() + String.format("%04d", RANDOM.nextInt(10_000));
    }

}
