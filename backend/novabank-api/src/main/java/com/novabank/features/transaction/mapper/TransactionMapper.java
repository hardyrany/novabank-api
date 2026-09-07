package com.novabank.features.transaction.mapper;

import org.springframework.stereotype.Component;
import com.novabank.features.transaction.dto.TransactionResponse;
import com.novabank.features.transaction.entity.Transaction;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {

        if (transaction == null) {
            return null;
        }

        TransactionResponse transactionResponse = new TransactionResponse();

        transactionResponse.setId(transaction.getId());
        transaction.setAccountId(transaction.getAccountId());
        transactionResponse.setTransactionType(transaction.getTransactionType().name());
        transactionResponse.setAmount(transaction.getAmount());
        transactionResponse.setCurrency(transaction.getCurrency());
        transactionResponse.setBalanceAfter(transaction.getBalanceAfter());
        transactionResponse.setDescription(transaction.getDescription());
        transactionResponse.setCreatedAt(transaction.getCreatedAt());

        return transactionResponse;
    }
}
