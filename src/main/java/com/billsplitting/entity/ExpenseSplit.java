package com.billsplitting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_splits", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"expense_id", "member_id"})
})
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private GroupMember member;

    @NotNull(message = "Split amount cannot be null")
    @DecimalMin(value = "0.00", message = "Split amount cannot be negative")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @DecimalMin(value = "0.00", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Percentage cannot exceed 100")
    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public ExpenseSplit() {}

    public ExpenseSplit(Expense expense, GroupMember member, BigDecimal amount) {
        this.expense = expense;
        this.member = member;
        this.amount = amount;
    }

    public ExpenseSplit(Expense expense, GroupMember member, BigDecimal amount, BigDecimal percentage) {
        this.expense = expense;
        this.member = member;
        this.amount = amount;
        this.percentage = percentage;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public GroupMember getMember() {
        return member;
    }

    public void setMember(GroupMember member) {
        this.member = member;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}