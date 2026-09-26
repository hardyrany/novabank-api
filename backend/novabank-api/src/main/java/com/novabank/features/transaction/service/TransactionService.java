package com.novabank.features.transaction.service;

import com.novabank.features.account.entity.Account;
import com.novabank.features.account.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.novabank.features.transaction.entity.Transaction;
import com.novabank.features.transaction.enums.TransactionType;
import com.novabank.features.transaction.repository.TransactionRepository;
import com.novabank.infra.exception.ResourceNotFoundException;
import com.novabank.infra.security.OwnershipValidator;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OwnershipValidator ownershipValidator;

    public TransactionService(TransactionRepository transactionRepository,
            AccountRepository accountRepository, OwnershipValidator ownershipValidator) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.ownershipValidator = ownershipValidator;
    }

    public Transaction transactionRecordEntry(Long accountId, TransactionType transactionType,
            BigDecimal amount, BigDecimal balanceAfter, String description) {

        Transaction transaction = new Transaction();

        transaction.setAccountId(accountId);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getHistoryByAccountId(Long accountId) {

        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found with id: " + accountId));

        ownershipValidator.validateAccountOwnership(account);

        return transactionRepository.findByAccountIdOrderByCreatedAtDescIdDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions(Long accountId) {
        return transactionRepository.findTop10ByAccountIdOrderByCreatedAtDescIdDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getHistoryByAccountIdAndType(Long accountId,
            TransactionType transactionType) {
        return transactionRepository.findByAccountIdAndTransactionTypeOrderByCreatedAtDescIdDesc(
                accountId, transactionType);
    }
}
