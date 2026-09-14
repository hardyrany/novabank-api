package com.novabank.features.transfer.dto;

import java.math.BigDecimal;

public class TransferRequest {

    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private String description;

    public TransferRequest() {}

    public TransferRequest(Long sourceAccountId, Long targetAccountId, BigDecimal amount,
            String description) {

        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.description = description;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(Long targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
