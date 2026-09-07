package com.novabank.features.account.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.novabank.features.account.dto.AccountRequest;
import com.novabank.features.account.dto.AccountResponse;
import com.novabank.features.account.dto.CustomerAccountSummary;
import com.novabank.features.account.dto.DepositRequest;
import com.novabank.features.account.dto.WithdrawRequest;
import com.novabank.features.account.entity.Account;
import com.novabank.features.account.mapper.AccountMapper;
import com.novabank.features.account.service.AccountService;
import com.novabank.features.transaction.dto.TransactionResponse;
import com.novabank.features.transaction.entity.Transaction;
import com.novabank.features.transaction.mapper.TransactionMapper;
import com.novabank.features.transaction.service.TransactionService;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper,
            TransactionService transactionService, TransactionMapper transactionMapper) {

        this.accountService = accountService;
        this.accountMapper = accountMapper;
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;

    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestBody AccountRequest accountRequest) {

        Account account = accountMapper.toEntity(accountRequest);
        Account savedAccount = accountService.createAccount(account);
        AccountResponse accountResponse = accountMapper.toResponse(savedAccount);

        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);

    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {

        Account account = accountService.getAccountById(id);
        AccountResponse accountResponse = accountMapper.toResponse(account);

        return ResponseEntity.ok(accountResponse);
    }

    @GetMapping("/account-number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByAccountNumber(
            @PathVariable String accountNumber) {

        Account account = accountService.getAccountByAccountNumber(accountNumber);
        AccountResponse accountResponse = accountMapper.toResponse(account);

        return ResponseEntity.ok(accountResponse);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(
            @PathVariable Long customerId) {

        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);

        List<AccountResponse> accountResponses =
                accounts.stream().map(accountMapper::toResponse).collect(Collectors.toList());

        return ResponseEntity.ok(accountResponses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<AccountResponse>> getActiveAccounts() {

        List<Account> accounts = accountService.getActiveAccounts();

        List<AccountResponse> accountResponses =
                accounts.stream().map(accountMapper::toResponse).collect(Collectors.toList());

        return ResponseEntity.ok(accountResponses);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable Long id) {

        Account account = accountService.getAccountById(id);

        return ResponseEntity.ok(account.getBalance());
    }

    @GetMapping("/customer/{customerId}/account-summary")
    public ResponseEntity<CustomerAccountSummary> getCustomerAccountSummary(
            @PathVariable Long customerId) {

        CustomerAccountSummary accountSummary = accountService.getCustomerSummary(customerId);

        return ResponseEntity.ok(accountSummary);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable Long id,
            @RequestBody AccountRequest accountRequest) {

        Account account = accountMapper.toEntity(accountRequest);
        Account updatedAccount = accountService.updateAccount(id, account);
        AccountResponse accountResponse = accountMapper.toResponse(updatedAccount);

        return ResponseEntity.ok(accountResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateAccount(@PathVariable Long id) {

        accountService.deactivateAccount(id);

        return ResponseEntity.noContent().build();

    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<AccountResponse> activateAccount(@PathVariable Long id) {

        accountService.activateAccount(id);

        Account account = accountService.getAccountById(id);
        AccountResponse accountResponse = accountMapper.toResponse(account);

        return ResponseEntity.ok(accountResponse);

    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<AccountResponse> deposit(@PathVariable Long id,
            @RequestBody DepositRequest depositRequest) {

        Account updatedAccount = accountService.deposit(id, depositRequest.getAmount(),
                depositRequest.getDescription());

        AccountResponse accountResponse = accountMapper.toResponse(updatedAccount);

        return ResponseEntity.ok(accountResponse);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable Long id,
            @RequestBody WithdrawRequest withdrawRequest) {

        Account updatedAccount = accountService.withdraw(id, withdrawRequest.getAmount(),
                withdrawRequest.getDescription());
        AccountResponse accountResponse = accountMapper.toResponse(updatedAccount);

        return ResponseEntity.ok(accountResponse);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionResponse(@PathVariable Long id) {

        List<Transaction> transactions = transactionService.getHistoryByAccountId(id);
        List<TransactionResponse> transactionResponses =
                transactionMapper.toResponseList(transactions);

        return ResponseEntity.ok(transactionResponses);
    }


}
