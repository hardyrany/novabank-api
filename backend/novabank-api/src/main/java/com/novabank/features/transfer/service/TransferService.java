package com.novabank.features.transfer.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.novabank.features.account.service.AccountService;
import com.novabank.features.transaction.service.TransactionService;

@Service
@Transactional
public class TransferService {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public TransferService(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public void transfer(Long sourceAccountId, Long targetAccountId, BigDecimal amount,
            String description) {

    }
}
