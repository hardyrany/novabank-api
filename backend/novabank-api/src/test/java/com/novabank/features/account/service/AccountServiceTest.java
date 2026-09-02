package com.novabank.features.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.novabank.features.account.entity.Account;
import com.novabank.features.account.repository.AccountRepository;
import com.novabank.features.customer.repository.CustomerRepository;
import com.novabank.features.customer.service.CustomerService;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setCustomerId(1L);
        account.setAccountNumber("ACC-1234567890");
        account.setAccountType("CHECKING");
        account.setBalance(java.math.BigDecimal.ZERO);
        account.setCurrency("USD");
        account.setActive(true);
    }

    @Test
    void createAccount_ShouldReturnSavedAccount_WhenSuccessful() {
        // Arrange
        when(customerService.getCustomerById(1L)).thenReturn(null); // Cliente existe
        when(accountRepository.findByAccountNumber("ACC-1234567890")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        Account result = accountService.createAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals(account.getId(), result.getId());
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
        assertEquals(account.getAccountType(), result.getAccountType());

        verify(customerService).getCustomerById(1L);
        verify(accountRepository).findByAccountNumber("ACC-1234567890");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_ShouldThrowBusinessException_WhenAccountNumberAlreadyExists() {
        // Arrange
        when(customerService.getCustomerById(1L)).thenReturn(null);
        when(accountRepository.findByAccountNumber("ACC-1234567890"))
                .thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(BusinessException.class, () -> accountService.createAccount(account));

        verify(customerService).getCustomerById(1L);
        verify(accountRepository).findByAccountNumber("ACC-1234567890");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_ShouldThrowResourceNotFoundException_WhenCustomerNotFound() {
        // Arrange
        when(customerService.getCustomerById(1L))
                .thenThrow(new ResourceNotFoundException("Customer not found with id: 1"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> accountService.createAccount(account));

        verify(customerService).getCustomerById(1L);
        verify(accountRepository, never()).findByAccountNumber(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountById_ShouldReturnAccount_WhenFound() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Act
        Account result = accountService.getAccountById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(account.getAccountNumber(), result.getAccountNumber());

        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccountById_ShouldThrowResourceNotFoundException_WhenNotFound() {
        // Arrange
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountService.getAccountById(99L));

        assertEquals("Account not found with id: 99", exception.getMessage());

        verify(accountRepository).findById(99L);
    }

    @Test
    void getAccountByAccountNumber_ShouldReturnAccount_WhenFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC-1234567890"))
                .thenReturn(Optional.of(account));

        // Act
        Account result = accountService.getAccountByAccountNumber("ACC-1234567890");

        // Assert
        assertNotNull(result);
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
        assertEquals(account.getAccountType(), result.getAccountType());

        verify(accountRepository).findByAccountNumber("ACC-1234567890");
    }
}
