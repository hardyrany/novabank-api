package com.novabank.features.transfer.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.novabank.features.account.entity.Account;
import com.novabank.features.account.service.AccountService;
import com.novabank.features.transaction.enums.TransactionType;
import com.novabank.features.transaction.service.TransactionService;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ConflictException;
import com.novabank.infra.security.OwnershipValidator;

@Service
@Transactional
public class TransferService {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final OwnershipValidator ownershipValidator;

    public TransferService(AccountService accountService, TransactionService transactionService,
            OwnershipValidator ownershipValidator) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.ownershipValidator = ownershipValidator;
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

        ownershipValidator.validateAccountOwnership(sourceAccount);

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
        accountService.saveAccount(sourceAccount);

        BigDecimal newTargetBalance = targetAccount.getBalance().add(amount);
        targetAccount.setBalance(newTargetBalance);
        accountService.saveAccount(targetAccount);

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

}