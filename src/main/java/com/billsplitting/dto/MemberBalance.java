package com.billsplitting.dto;

import java.math.BigDecimal;

public class MemberBalance {
    private String memberName;
    private BigDecimal totalPaid;
    private BigDecimal totalOwed;
    private BigDecimal netBalance;

    public MemberBalance(String memberName, BigDecimal totalPaid, BigDecimal totalOwed) {
        this.memberName = memberName;
        this.totalPaid = totalPaid;
        this.totalOwed = totalOwed;
        this.netBalance = totalPaid.subtract(totalOwed);
    }

    // Getters and Setters
    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
        this.netBalance = this.totalPaid.subtract(this.totalOwed);
    }

    public BigDecimal getTotalOwed() {
        return totalOwed;
    }

    public void setTotalOwed(BigDecimal totalOwed) {
        this.totalOwed = totalOwed;
        this.netBalance = this.totalPaid.subtract(this.totalOwed);
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public boolean isCreditor() {
        return netBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isDebtor() {
        return netBalance.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isSettled() {
        return netBalance.compareTo(BigDecimal.ZERO) == 0;
    }
}