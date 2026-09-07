package com.novabank.features.account.mapper;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import com.novabank.features.account.dto.AccountRequest;
import com.novabank.features.account.dto.AccountResponse;
import com.novabank.features.account.entity.Account;

@Component
public class AccountMapper {

    public Account toEntity(AccountRequest accountRequest) {

        Account account = new Account();

        account.setCustomerId(accountRequest.getCustomerId());
        account.setAccountNumber(accountRequest.getAccountNumber());
        account.setAccountType(accountRequest.getAccountType());
        account.setBalance(
                accountRequest.getInitialBalance() != null ? accountRequest.getInitialBalance()
                        : BigDecimal.ZERO);
        account.setCurrency(
                accountRequest.getCurrency() != null ? accountRequest.getCurrency() : "USD");

        return account;
    }

    public AccountResponse toResponse(Account account) {

        AccountResponse accountResponse = new AccountResponse();

        accountResponse.setId(account.getId());
        accountResponse.setCustomerId(account.getCustomerId());
        accountResponse.setAccountNumber(account.getAccountNumber());
        accountResponse.setAccountType(account.getAccountType());
        accountResponse.setBalance(account.getBalance());
        accountResponse.setCurrency(account.getCurrency());
        accountResponse.setActive(account.isActive());
        accountResponse.setCreatedAt(account.getCreatedAt());
        accountResponse.setUpdatedAt(account.getUpdatedAt());

        return accountResponse;
    }
}
