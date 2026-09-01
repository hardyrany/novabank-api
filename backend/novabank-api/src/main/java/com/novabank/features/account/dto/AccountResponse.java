package com.novabank.features.account.dto;

public class AccountResponse {

    private Long customerId;
    private String accountNumber;
    private String accountType;

    public AccountResponse(Long customerId, String accountNumber, String accountType) {
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
