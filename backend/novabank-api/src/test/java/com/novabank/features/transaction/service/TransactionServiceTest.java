package com.novabank.features.transaction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
}
