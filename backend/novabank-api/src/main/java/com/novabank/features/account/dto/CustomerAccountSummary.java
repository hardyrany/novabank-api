package com.novabank.features.account.dto;

import java.math.BigDecimal;
import java.util.Map;

public class CustomerAccountSummary {

    private Long customerId;
    private BigDecimal totalBalance;
    private int totalAccounts;
    private int activeAccount;
    private int inactiveAccount;
    private Map<String, BigDecimal> balanceByCurrency;
    private Map<String, Integer> accountTypes;

    public CustomerAccountSummary() {

    }

    public CustomerAccountSummary(Long customerId, BigDecimal totalBalance, int totalAccounts,
            int activateAccount, int inactiveAccount, Map<String, BigDecimal> balanceByCurrency,
            Map<String, Integer> accountTypes) {

        this.customerId = customerId;
        this.totalBalance = totalBalance;
        this.totalAccounts = totalAccounts;
        this.activeAccount = activateAccount;
        this.inactiveAccount = inactiveAccount;
        this.balanceByCurrency = balanceByCurrency;
        this.accountTypes = accountTypes;

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

    public Map<String, BigDecimal> getBalanceByCurrency() {
        return balanceByCurrency;
    }

    public void setBalanceByCurrency(Map<String, BigDecimal> balanceByCurrency) {
        this.balanceByCurrency = balanceByCurrency;
    }

    public Map<String, Integer> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(Map<String, Integer> accountTypes) {
        this.accountTypes = accountTypes;
    }

}
