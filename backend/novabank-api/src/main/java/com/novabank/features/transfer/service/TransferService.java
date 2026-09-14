package com.novabank.features.transfer.service;

import java.math.BigDecimal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.novabank.features.account.entity.Account;
import com.novabank.features.account.service.AccountService;
import com.novabank.features.customer.entity.Customer;
import com.novabank.features.customer.repository.CustomerRepository;
import com.novabank.features.transaction.enums.TransactionType;
import com.novabank.features.transaction.service.TransactionService;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ConflictException;
import com.novabank.infra.exception.ForbiddenException;

@Service
@Transactional
public class TransferService {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CustomerRepository customerRepository;

    public TransferService(AccountService accountService, TransactionService transactionService,
            CustomerRepository customerRepository) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.customerRepository = customerRepository;
    }

    public void transfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount,
            String description) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be greater than zero");
        }

        if (sourceAccountId.equals(targetAccountId)) {
            throw new BusinessException("Source and target accounts must be different");
        }

        Account sourceAccount = accountService.getAccountById(sourceAccountId);
        Account targetAccount = accountService.getAccountById(targetAccountId);

        validateOwnerShip(sourceAccount);

        if (!sourceAccount.isActive()) {
            throw new BusinessException("Source account is not active");
        }

        if (!targetAccount.isActive()) {
            throw new BusinessException("Target account is not active");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new ConflictException("Insufficient balance for transfer");
        }

        BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(amount);
        sourceAccount.setBalance(newSourceBalance);
        accountService.updateAccount(sourceAccountId, sourceAccount);

        BigDecimal newTargetBalance = targetAccount.getBalance().add(amount);
        targetAccount.setBalance(newTargetBalance);
        accountService.updateAccount(targetAccountId, targetAccount);

        String debitDescription =
                description != null ? "Transfer to account " + targetAccountId + " - " + description
                        : "Transfer to account " + targetAccountId;

        transactionService.transactionRecordEntry(sourceAccountId, TransactionType.TRANSFER_OUT,
                amount, newSourceBalance, debitDescription);

        String creditDescription = description != null
                ? "Transfer from account " + sourceAccountId + " - " + description
                : "Transfer from account " + sourceAccountId;

        transactionService.transactionRecordEntry(targetAccountId, TransactionType.TRANSFER_IN,
                amount, newTargetBalance, creditDescription);
    }

    private void  validateOwnerShip(Account sourceAccount) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Customer customer = customerRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ForbiddenException("No Customer associated with authenticated user"));

        if (!sourceAccount.getCustomerId().equals(customer.getId())) {
            throw new ForbiddenException("Account " + sourceAccount.getId() + "            does not belong to the authenticated customer");
        }
    }

}
