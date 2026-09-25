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
import com.novabank.infra.exception.ConflictException;
import com.novabank.infra.exception.ForbiddenException;
import com.novabank.infra.exception.ResourceNotFoundException;
import com.novabank.infra.security.OwnershipValidator;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private OwnershipValidator ownershipValidator;

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
        sourceAccount.setCustomerId(1L);
        sourceAccount.setBalance(BigDecimal.valueOf(500.00));

        targetAccount = new Account();
        targetAccount.setId(targetAccountId);
        targetAccount.setCustomerId(2L);
        targetAccount.setBalance(BigDecimal.valueOf(200.00));
    }

    @Test
    void transfer_ShouldSucceed_WhenValidRequest() {
        BigDecimal newSourceBalance = BigDecimal.valueOf(400.00);
        BigDecimal newTargetBalance = BigDecimal.valueOf(300.00);

        when(accountService.getAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountById(targetAccountId)).thenReturn(targetAccount);
        when(accountService.updateAccount(eq(sourceAccountId), any(Account.class)))
                .thenReturn(sourceAccount);
        when(accountService.updateAccount(eq(targetAccountId), any(Account.class)))
                .thenReturn(targetAccount);

        transferService.transfer(sourceAccountId, targetAccountId, amount, description);

        verify(ownershipValidator).validateAccountOwnership(sourceAccount);
        verify(accountService).updateAccount(eq(sourceAccountId), any(Account.class));
        verify(accountService).updateAccount(eq(targetAccountId), any(Account.class));

        verify(transactionService).transactionRecordEntry(sourceAccountId,
                TransactionType.TRANSFER_OUT, amount, newSourceBalance,
                "Transfer to account 2 - Test transfer");

        verify(transactionService).transactionRecordEntry(targetAccountId,
                TransactionType.TRANSFER_IN, amount, newTargetBalance,
                "Transfer from account 1 - Test transfer");
    }

    @Test
    void transfer_ShouldThrowBusinessException_WhenAmountIsInvalid() {
        assertThrows(BusinessException.class, () -> transferService.transfer(sourceAccountId,
                targetAccountId, null, description));

        assertThrows(BusinessException.class, () -> transferService.transfer(sourceAccountId,
                targetAccountId, BigDecimal.ZERO, description));

        assertThrows(BusinessException.class, () -> transferService.transfer(sourceAccountId,
                targetAccountId, BigDecimal.valueOf(-100.00), description));

        verify(accountService, never()).getAccountById(anyLong());
        verify(ownershipValidator, never()).validateAccountOwnership(any(Account.class));
    }

    @Test
    void transfer_ShouldThrowBusinessException_WhenSourceAndTargetAccountsAreSame() {
        assertThrows(BusinessException.class, () -> transferService.transfer(sourceAccountId,
                sourceAccountId, amount, description));

        verify(accountService, never()).getAccountById(anyLong());
        verify(ownershipValidator, never()).validateAccountOwnership(any(Account.class));
    }

    @Test
    void transfer_ShouldThrowResourceNotFoundException_WhenSourceAccountNotFound() {
        when(accountService.getAccountById(sourceAccountId)).thenThrow(
                new ResourceNotFoundException("Account not found with id: " + sourceAccountId));

        assertThrows(ResourceNotFoundException.class, () -> transferService
                .transfer(sourceAccountId, targetAccountId, amount, description));

        verify(accountService).getAccountById(sourceAccountId);
        verify(ownershipValidator, never()).validateAccountOwnership(any(Account.class));
    }

    @Test
    void transfer_ShouldThrowResourceNotFoundException_WhenTargetAccountNotFound() {
        when(accountService.getAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountById(targetAccountId)).thenThrow(
                new ResourceNotFoundException("Account not found with id: " + targetAccountId));

        assertThrows(ResourceNotFoundException.class, () -> transferService
                .transfer(sourceAccountId, targetAccountId, amount, description));

        verify(accountService).getAccountById(sourceAccountId);
        verify(accountService).getAccountById(targetAccountId);
        verify(ownershipValidator, never()).validateAccountOwnership(any(Account.class));
    }

    @Test
    void transfer_ShouldThrowConflictException_WhenInsufficientBalance() {
        BigDecimal transferAmount = BigDecimal.valueOf(600.00);

        when(accountService.getAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountById(targetAccountId)).thenReturn(targetAccount);

        assertThrows(ConflictException.class, () -> transferService.transfer(sourceAccountId,
                targetAccountId, transferAmount, description));

        verify(accountService).getAccountById(sourceAccountId);
        verify(accountService).getAccountById(targetAccountId);
        verify(ownershipValidator).validateAccountOwnership(sourceAccount);
        verify(accountService, never()).updateAccount(anyLong(), any(Account.class));
    }

    @Test
    void transfer_ShouldThrowForbiddenException_WhenSourceAccountDoesNotBelongToCustomer() {
        when(accountService.getAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountById(targetAccountId)).thenReturn(targetAccount);

        doThrow(new ForbiddenException("Account does not belong to the authenticated customer"))
                .when(ownershipValidator).validateAccountOwnership(sourceAccount);

        assertThrows(ForbiddenException.class, () -> transferService.transfer(sourceAccountId,
                targetAccountId, amount, description));

        verify(accountService, never()).updateAccount(anyLong(), any(Account.class));
    }

    @Test
    void transfer_ShouldThrowForbiddenException_WhenNoCustomerFoundForAuthenticatedUser() {
        when(accountService.getAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountById(targetAccountId)).thenReturn(targetAccount);

        doThrow(new ForbiddenException("No customer associated with authenticated user"))
                .when(ownershipValidator).validateAccountOwnership(sourceAccount);

        assertThrows(ForbiddenException.class, () -> transferService.transfer(sourceAccountId,
                targetAccountId, amount, description));

        verify(accountService, never()).updateAccount(anyLong(), any(Account.class));
    }
}
