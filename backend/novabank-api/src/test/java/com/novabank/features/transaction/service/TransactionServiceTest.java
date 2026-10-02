package com.novabank.features.transaction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.novabank.features.account.entity.Account;
import com.novabank.features.account.repository.AccountRepository;
import com.novabank.features.transaction.entity.Transaction;
import com.novabank.features.transaction.enums.TransactionType;
import com.novabank.features.transaction.repository.TransactionRepository;
import com.novabank.infra.security.OwnershipValidator;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OwnershipValidator ownershipValidator;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Account account;
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

        account = new Account();
        account.setId(accountId);
        account.setCustomerId(1L);
        account.setBalance(BigDecimal.valueOf(1000.00));
    }

    @Test
    void recordEntry_ShouldSaveTransaction_WhenSuccessful() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.transactionRecordEntry(accountId,
                TransactionType.DEPOSIT, amount, balanceAfter, "Test deposit");

        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(TransactionType.DEPOSIT, result.getTransactionType());
        assertEquals(amount, result.getAmount());
        assertEquals(balanceAfter, result.getBalanceAfter());
        assertEquals("Test deposit", result.getDescription());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getRecentTransactions_ShouldReturnLast10Transactions_WhenAccountHasManyTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Transaction t = new Transaction();
            t.setId((long) i);
            t.setAccountId(accountId);
            t.setTransactionType(i % 2 == 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAW);
            t.setAmount(BigDecimal.valueOf(i * 10.00));
            t.setBalanceAfter(BigDecimal.valueOf(1000.00 - (i * 10.00)));
            t.setDescription("Transaction " + i);
            t.setCreatedAt(LocalDateTime.now().minusMinutes(10 - i));
            transactions.add(t);
        }
        transactions.sort((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()));

        when(transactionRepository.findTop10ByAccountIdOrderByCreatedAtDescIdDesc(accountId))
                .thenReturn(transactions);

        List<Transaction> result = transactionService.getRecentTransactions(accountId);

        assertNotNull(result);
        assertEquals(10, result.size());
        assertEquals("Transaction 10", result.get(0).getDescription());

        verify(transactionRepository).findTop10ByAccountIdOrderByCreatedAtDescIdDesc(accountId);
    }

    @Test
    void getHistoryByAccountIdAndType_ShouldFilterByType_WhenValidType() {
        List<Transaction> deposits = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Transaction t = new Transaction();
            t.setId((long) i);
            t.setAccountId(accountId);
            t.setTransactionType(TransactionType.DEPOSIT);
            t.setAmount(BigDecimal.valueOf(i * 100.00));
            t.setBalanceAfter(BigDecimal.valueOf(500.00 + (i * 100.00)));
            t.setDescription("Deposit " + i);
            t.setCreatedAt(LocalDateTime.now().minusMinutes(i));
            deposits.add(t);
        }

        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(deposits);

        when(transactionRepository.findByAccountIdAndTransactionTypeOrderByCreatedAtDescIdDesc(
                accountId, TransactionType.DEPOSIT, pageable)).thenReturn(page);

        Page<Transaction> result = transactionService.getHistoryByAccountIdAndType(accountId,
                TransactionType.DEPOSIT, pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertTrue(result.getContent().stream()
                .allMatch(t -> t.getTransactionType() == TransactionType.DEPOSIT));

        verify(transactionRepository).findByAccountIdAndTransactionTypeOrderByCreatedAtDescIdDesc(
                accountId, TransactionType.DEPOSIT, pageable);
    }

    @Test
    void getHistoryByAccountId_ShouldReturnTransactionList_WhenAccountHasTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Transaction t = new Transaction();
            t.setId((long) i);
            t.setAccountId(accountId);
            t.setTransactionType(i % 2 == 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAW);
            t.setAmount(BigDecimal.valueOf(i * 100.00));
            t.setBalanceAfter(BigDecimal.valueOf(500.00 + (i * 100.00)));
            t.setDescription("Transaction " + i);
            t.setCreatedAt(LocalDateTime.now().minusMinutes(i));
            transactions.add(t);
        }

        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(transactions);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountIdOrderByCreatedAtDescIdDesc(accountId, pageable))
                .thenReturn(page);

        Page<Transaction> result = transactionService.getHistoryByAccountId(accountId, pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals("Transaction 1", result.getContent().get(0).getDescription());

        verify(ownershipValidator).validateAccountOwnership(account);
        verify(transactionRepository).findByAccountIdOrderByCreatedAtDescIdDesc(accountId,
                pageable);
    }

    @Test
    void getHistoryByAccountId_ShouldReturnEmptyList_WhenAccountHasNoTransactions() {
        List<Transaction> emptyList = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(emptyList);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountIdOrderByCreatedAtDescIdDesc(accountId, pageable))
                .thenReturn(page);

        Page<Transaction> result = transactionService.getHistoryByAccountId(accountId, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());

        verify(ownershipValidator).validateAccountOwnership(account);
        verify(transactionRepository).findByAccountIdOrderByCreatedAtDescIdDesc(accountId,
                pageable);
    }
}
