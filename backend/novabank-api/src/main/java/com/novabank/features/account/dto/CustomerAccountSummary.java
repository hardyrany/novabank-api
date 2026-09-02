package com.novabank.features.account.dto;

import java.math.BigDecimal;

public class CustomerAccountSummary {

    private Long customerId;
    private BigDecimal totalBalance;
    private int totalAccounts;
    private int activeAccount;
    private int inactiveAccount;

    public CustomerAccountSummary() {

    }

    public CustomerAccountSummary(Long customerId, BigDecimal totalBalance, int totalAccounts,
            int activateAccount, int inactiveAccount) {

        this.customerId = customerId;
        this.totalBalance = totalBalance;
        this.totalAccounts = totalAccounts;
        this.activeAccount = activateAccount;
        this.inactiveAccount = inactiveAccount;

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

    public int getActiveAccount() {
        return activeAccount;
    }

    public void setActiveAccount(int activeAccount) {
        this.activeAccount = activeAccount;
    }

    public int getInactiveAccount() {
        return inactiveAccount;
    }

    public void setInactiveAccount(int inactiveAccount) {
        this.inactiveAccount = inactiveAccount;
    }

}
