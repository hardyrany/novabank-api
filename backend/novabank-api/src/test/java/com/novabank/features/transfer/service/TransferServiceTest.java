package com.novabank.features.transfer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.novabank.features.account.entity.Account;
import com.novabank.features.account.service.AccountService;
import com.novabank.features.transaction.enums.TransactionType;
import com.novabank.features.transaction.service.TransactionService;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransferService transferService;

    private Account sourceAccount;
    private Account targetAccount;
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private String description;

    @BeforeEach
    void setUp() {
        sourceAccountId = 1L;
        targetAccountId = 2L;
        amount = BigDecimal.valueOf(100.00);
        description = "Test transfer";

        sourceAccount = new Account();
        sourceAccount.setId(sourceAccountId);
        sourceAccount.setBalance(BigDecimal.valueOf(500.00));

        targetAccount = new Account();
        targetAccount.setId(targetAccountId);
        targetAccount.setBalance(BigDecimal.valueOf(200.00));
    }

    @Test
    void transfer_ShouldSucceed_WhenValidRequest() {
        // Arrange
        BigDecimal newSourceBalance = BigDecimal.valueOf(400.00);
        BigDecimal newTargetBalance = BigDecimal.valueOf(300.00);

        when(accountService.getAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountById(targetAccountId)).thenReturn(targetAccount);
        when(accountService.updateAccount(eq(sourceAccountId), any(Account.class)))
                .thenReturn(sourceAccount);
        when(accountService.updateAccount(eq(targetAccountId), any(Account.class)))
                .thenReturn(targetAccount);

        // Act
        transferService.transfer(sourceAccountId, targetAccountId, amount, description);

        // Assert
        verify(accountService).getAccountById(sourceAccountId);
        verify(accountService).getAccountById(targetAccountId);

        verify(accountService).updateAccount(eq(sourceAccountId), any(Account.class));
        verify(accountService).updateAccount(eq(targetAccountId), any(Account.class));

        verify(transactionService).transactionRecordEntry(sourceAccountId,
                TransactionType.TRANSFER_OUT, amount, newSourceBalance,
                "Transfer to account 2 - Test transfer");

        verify(transactionService).transactionRecordEntry(targetAccountId,
                TransactionType.TRANSFER_IN, amount, newTargetBalance,
                "Transfer from account 1 - Test transfer");
    }

}
