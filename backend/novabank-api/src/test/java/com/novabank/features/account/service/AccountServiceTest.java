package com.novabank.features.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.List;
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

    @Test
    void getAccountByAccountNumber_ShouldThrowResourceNotFoundException_WhenNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("INVALID-123")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountService.getAccountByAccountNumber("INVALID-123"));

        assertEquals("Account not found with number: INVALID-123", exception.getMessage());

        verify(accountRepository).findByAccountNumber("INVALID-123");
    }

    @Test
    void getAccountsByCustomerId_ShouldThrowResourceNotFoundException_WhenCustomerNotFound() {
        // Arrange
        when(customerService.getCustomerById(99L))
                .thenThrow(new ResourceNotFoundException("Customer not found with id: 99"));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountService.getAccountsByCustomerId(99L));

        assertEquals("Customer not found with id: 99", exception.getMessage());

        verify(customerService).getCustomerById(99L);
        verify(accountRepository, never()).findByCustomerId(99L);
    }

    @Test
    void getActiveAccounts_ShouldReturnOnlyActiveAccounts_WhenCalled() {
        // Arrange
        when(accountRepository.findByIsActive(true)).thenReturn(List.of(account));

        // Act
        List<Account> result = accountService.getActiveAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());

        verify(accountRepository).findByIsActive(true);
    }

    @Test
    void updateAccount_ShouldUpdateAndReturnAccount_WhenSuccessful() {
        // Arrange
        Account existingAccount = new Account();
        existingAccount.setId(1L);
        existingAccount.setCustomerId(1L);
        existingAccount.setAccountNumber("ACC-1234567890");
        existingAccount.setAccountType("CHECKING");
        existingAccount.setCurrency("USD");
        existingAccount.setBalance(java.math.BigDecimal.valueOf(1000.00));

        Account updatedDetails = new Account();
        updatedDetails.setAccountType("SAVINGS");
        updatedDetails.setCurrency("EUR");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(existingAccount)).thenReturn(existingAccount);

        // Act
        Account result = accountService.updateAccount(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals("SAVINGS", result.getAccountType());
        assertEquals("EUR", result.getCurrency());
        // Verifica que outros campos permanecem inalterados
        assertEquals(1L, result.getCustomerId());
        assertEquals("ACC-1234567890", result.getAccountNumber());

        verify(accountRepository).findById(1L);
        verify(accountRepository).save(existingAccount);
    }

    @Test
    void updateAccount_ShouldThrowResourceNotFoundException_WhenAccountNotFound() {
        // Arrange
        Account updatedDetails = new Account();
        updatedDetails.setAccountType("SAVINGS");
        updatedDetails.setCurrency("EUR");

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountService.updateAccount(99L, updatedDetails));

        assertEquals("Account not found with id: 99", exception.getMessage());

        verify(accountRepository).findById(99L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deactivateAccount_ShouldSetActiveToFalse_WhenSuccessful() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        accountService.deactivateAccount(1L);

        // Assert
        assertFalse(account.isActive()); // Verifica se a conta foi desativada
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(account);
    }

    @Test
    void deactivateAccount_ShouldThrowResourceNotFoundException_WhenAccountNotFound() {
        // Arrange
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountService.deactivateAccount(99L));

        assertEquals("Account not found with id: 99", exception.getMessage());

        verify(accountRepository).findById(99L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void activateAccount_ShouldSetActiveToTrue_WhenSuccessful() {
        // Arrange
        account.setActive(false); // Conta está desativada
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        accountService.activateAccount(1L);

        // Assert
        assertTrue(account.isActive()); // Verifica se a conta foi reativada
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(account);
    }

    @Test
    void activateAccount_ShouldThrowResourceNotFoundException_WhenAccountNotFound() {
        // Arrange
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountService.activateAccount(99L));

        assertEquals("Account not found with id: 99", exception.getMessage());

        verify(accountRepository).findById(99L);
        verify(accountRepository, never()).save(any(Account.class));
    }
}
