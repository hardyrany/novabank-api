package com.novabank.features.account.mapper;

import org.springframework.stereotype.Component;
import com.novabank.features.account.dto.AccountRequest;
import com.novabank.features.account.entity.Account;

@Component
public class AccountMapper {

    public Account toEntity(AccountRequest accountRequest) {

        Account account = new Account();

        account.setCustomerId(accountRequest.getCustomerId());
        account.setAccountNumber(accountRequest.getAccountNumber());
        account.setAccountType(accountRequest.getAccountType());

        return account;
    }
}
