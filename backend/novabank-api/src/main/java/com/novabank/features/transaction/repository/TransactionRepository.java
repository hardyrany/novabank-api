package com.novabank.features.transaction.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.novabank.features.transaction.enums.TransactionType;
import jakarta.transaction.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<Transaction> findByAccountIdAndTransactionTypeOrderByCreatedAtDesc(Long accountId,
            TransactionType transactionType);

    List<Transaction> findTop10ByAccountIdOrderByCreatedAtDesc(Long accountId);
}
