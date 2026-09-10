package com.novabank.features.transaction.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.novabank.features.transaction.entity.Transaction;
import com.novabank.features.transaction.enums.TransactionType;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByCreatedAtDescIdDesc(Long accountId);

    List<Transaction> findByAccountIdAndTransactionTypeOrderByCreatedAtDescIdDesc(Long accountId,
            TransactionType transactionType);

    List<Transaction> findTop10ByAccountIdOrderByCreatedAtDescIdDesc(Long accountId);
}
