package com.billsplitting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    @NotBlank(message = "Expense description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_member_id", nullable = false)
    private GroupMember paidByMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false)
    private SplitType splitType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseSplit> splits = new ArrayList<>();

    // Constructors
    public Expense() {}

    public Expense(ExpenseGroup group, String description, BigDecimal amount, GroupMember paidByMember) {
        this.group = group;
        this.description = description;
        this.amount = amount;
        this.paidByMember = paidByMember;
        this.splitType = SplitType.EQUAL; // Default split type
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpenseGroup getGroup() {
        return group;
    }

    public void setGroup(ExpenseGroup group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public GroupMember getPaidByMember() {
        return paidByMember;
    }

    public void setPaidByMember(GroupMember paidByMember) {
        this.paidByMember = paidByMember;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public void setSplitType(SplitType splitType) {
        this.splitType = splitType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ExpenseSplit> getSplits() {
        return splits;
    }

    public void setSplits(List<ExpenseSplit> splits) {
        this.splits = splits;
    }

    // Helper methods
    public void addSplit(ExpenseSplit split) {
        splits.add(split);
        split.setExpense(this);
    }

    public void removeSplit(ExpenseSplit split) {
        splits.remove(split);
        split.setExpense(null);
    }
}