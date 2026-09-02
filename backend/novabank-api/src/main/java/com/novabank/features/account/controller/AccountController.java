package com.novabank.features.account.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.novabank.features.account.dto.AccountRequest;
import com.novabank.features.account.dto.AccountResponse;
import com.novabank.features.account.entity.Account;
import com.novabank.features.account.mapper.AccountMapper;
import com.novabank.features.account.service.AccountService;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {

        this.accountService = accountService;
        this.accountMapper = accountMapper;

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
                accounts.stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(accountResponses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<AccountResponse>> getActiveAccounts() {

        List<Account> accounts = accountService.getActiveAccounts();

        List<AccountResponse> accountResponses =
                accounts.stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(accountResponses);
    }
}
