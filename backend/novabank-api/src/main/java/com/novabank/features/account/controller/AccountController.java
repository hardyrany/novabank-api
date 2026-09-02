package com.novabank.features.account.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

}
