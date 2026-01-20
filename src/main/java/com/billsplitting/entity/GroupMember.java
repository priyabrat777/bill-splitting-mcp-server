package com.billsplitting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"group_id", "member_name"})
})
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    @NotBlank(message = "Member name cannot be blank")
    @Size(max = 255, message = "Member name cannot exceed 255 characters")
    @Column(name = "member_name", nullable = false)
    private String memberName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "paidByMember", cascade = CascadeType.ALL)
    private List<Expense> paidExpenses = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseSplit> expenseSplits = new ArrayList<>();

    // Constructors
    public GroupMember() {}

    public GroupMember(ExpenseGroup group, String memberName) {
        this.group = group;
        this.memberName = memberName;
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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Expense> getPaidExpenses() {
        return paidExpenses;
    }

    public void setPaidExpenses(List<Expense> paidExpenses) {
        this.paidExpenses = paidExpenses;
    }

    public List<ExpenseSplit> getExpenseSplits() {
        return expenseSplits;
    }

    public void setExpenseSplits(List<ExpenseSplit> expenseSplits) {
        this.expenseSplits = expenseSplits;
    }
}