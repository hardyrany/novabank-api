package com.novabank.features.account.service;

import com.novabank.features.account.dto.CustomerAccountSummary;
import com.novabank.features.account.entity.Account;
import com.novabank.features.account.repository.AccountRepository;
import com.novabank.features.customer.repository.CustomerRepository;
import com.novabank.features.customer.service.CustomerService;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

        return accountRepository.findByIsActive(true);
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

    public void deactivateAccount(Long id) {

        Account account = getAccountById(id);

        account.setActive(false);
        accountRepository.save(account);
    }

    public void activateAccount(Long id) {

        Account account = getAccountById(id);

        account.setActive(true);
        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public CustomerAccountSummary getCustomerSummary(Long customerId) {

        customerService.getCustomerById(customerId);

        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        CustomerAccountSummary accountSummary = new CustomerAccountSummary();
        accountSummary.setCustomerId(customerId);

        accountSummary.setTotalAccounts(accounts.size());

        long activeCount = accounts.stream().filter(Account::isActive).count();
        accountSummary.setActiveAccounts((int) activeCount);
        accountSummary.setInactiveAccounts(accounts.size() - (int) activeCount);

        BigDecimal totalBalance =
                accounts.stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        accountSummary.setTotalBalance(totalBalance);

        Map<String, BigDecimal> balanceByCurrency = accounts.stream().collect(Collectors.groupingBy(
                Account::getCurrency,
                Collectors.reducing(BigDecimal.ZERO, Account::getBalance, BigDecimal::add)));

        Map<String, Integer> accountByType =
                accounts.stream().collect(Collectors.groupingBy(Account::getAccountType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        accountSummary.setAccountTypes(accountByType);

        return accountSummary;
    }

    private String generateAccountNumber() {
        return System.currentTimeMillis() + String.format("%04d", RANDOM.nextInt(10_000));
    }

}
