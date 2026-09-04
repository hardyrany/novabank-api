package com.novabank.features.transaction.service;

import com.novabank.features.customer.repository.CustomerRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.novabank.features.transaction.entity.Transaction;
import com.novabank.features.transaction.enums.TransactionType;
import com.novabank.features.transaction.repository.TransactionRepository;

@Service
@Transactional
public class TransactionService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository,
            CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
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
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions(Long accountId) {
        return transactionRepository.findTop10ByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getHistoryByAccountIdAndType(Long accountId,
            TransactionType transactionType) {
        return transactionRepository
                .findByAccountIdAndTransactionTypeOrderByCreatedAtDesc(accountId, transactionType);
    }
}
