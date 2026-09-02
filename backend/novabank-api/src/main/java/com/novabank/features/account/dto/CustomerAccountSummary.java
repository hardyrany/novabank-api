package com.novabank.features.account.dto;

import java.math.BigDecimal;
import java.util.Map;

public class CustomerAccountSummary {

    private Long customerId;
    private BigDecimal totalBalance;
    private int totalAccounts;
    private int activeAccounts;
    private int inactiveAccounts;
    private Map<String, BigDecimal> balanceByCurrency;
    private Map<String, Integer> accountTypes;

    public CustomerAccountSummary() {

    }

    public CustomerAccountSummary(Long customerId, BigDecimal totalBalance, int totalAccounts,
            int activateAccounts, int inactiveAccounts, Map<String, BigDecimal> balanceByCurrency,
            Map<String, Integer> accountTypes) {

        this.customerId = customerId;
        this.totalBalance = totalBalance;
        this.totalAccounts = totalAccounts;
        this.activeAccounts = activateAccounts;
        this.inactiveAccounts = inactiveAccounts;
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

    public int getActiveAccounts() {
        return activeAccounts;
    }

    public void setActiveAccounts(int activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public int getInactiveAccounts() {
        return inactiveAccounts;
    }

    public void setInactiveAccounts(int inactiveAccounts) {
        this.inactiveAccounts = inactiveAccounts;
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
