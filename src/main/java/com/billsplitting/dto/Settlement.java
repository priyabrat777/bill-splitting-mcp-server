package com.billsplitting.dto;

import java.math.BigDecimal;

public class Settlement {
    private String fromMember;
    private String toMember;
    private BigDecimal amount;

    public Settlement(String fromMember, String toMember, BigDecimal amount) {
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.amount = amount;
    }

    // Getters and Setters
    public String getFromMember() {
        return fromMember;
    }

    public void setFromMember(String fromMember) {
        this.fromMember = fromMember;
    }

    public String getToMember() {
        return toMember;
    }

    public void setToMember(String toMember) {
        this.toMember = toMember;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("%s pays ₹%.2f to %s", fromMember, amount, toMember);
    }
}