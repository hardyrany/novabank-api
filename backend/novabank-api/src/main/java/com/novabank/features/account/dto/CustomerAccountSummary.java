package com.novabank.features.account.dto;

import java.math.BigDecimal;

public class CustomerAccountSummary {

    private Long customerId;
    private BigDecimal totalBalance;
    private int totalAccounts;


    public CustomerAccountSummary(Long customerId, BigDecimal totalBalance, int totalAccounts) {

        this.customerId = customerId;
        this.totalBalance = totalBalance;
        this.totalAccounts = totalAccounts;

    }


    public Long getCustomerId() {
        return customerId;
    }


    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }


    public BigDecimal getTotalBalance() {
        return totalBalance;
    }


    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }


    public int getTotalAccounts() {
        return totalAccounts;
    }


    public void setTotalAccounts(int totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

}
