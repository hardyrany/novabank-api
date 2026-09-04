package com.novabank.features.transaction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.novabank.features.transaction.entity.Transaction;
import com.novabank.features.transaction.enums.TransactionType;
import com.novabank.features.transaction.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Long accountId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;

    @BeforeEach
    void setUp() {
        accountId = 1L;
        amount = BigDecimal.valueOf(100.00);
        balanceAfter = BigDecimal.valueOf(500.00);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountId(accountId);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setCurrency("USD");
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription("Test deposit");
        transaction.setCreatedAt(LocalDateTime.now());

    }

    @Test
    void recordEntry_ShouldSaveTransaction_WhenSuccessful() {
        // Arrange
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        Transaction result = transactionService.transactionRecordEntry(accountId,
                TransactionType.DEPOSIT, amount, balanceAfter, "Test deposit");

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(TransactionType.DEPOSIT, result.getTransactionType());
        assertEquals(amount, result.getAmount());
        assertEquals(balanceAfter, result.getBalanceAfter());
        assertEquals("Test deposit", result.getDescription());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getHistoryByAccountId_ShouldReturnTransactionList_WhenAccountHasTransactions() {
        // Arrange
        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setAccountId(accountId);
        transaction2.setTransactionType(TransactionType.WITHDRAW);
        transaction2.setAmount(BigDecimal.valueOf(50.00));
        transaction2.setBalanceAfter(BigDecimal.valueOf(450.00));
        transaction2.setDescription("Test withdraw");
        transaction2.setCreatedAt(LocalDateTime.now());

        List<Transaction> transactions = Arrays.asList(transaction, transaction2);

        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId))
                .thenReturn(transactions);

        // Act
        List<Transaction> result = transactionService.getHistoryByAccountId(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TransactionType.DEPOSIT, result.get(0).getTransactionType());
        assertEquals(TransactionType.WITHDRAW, result.get(1).getTransactionType());

        verify(transactionRepository).findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
