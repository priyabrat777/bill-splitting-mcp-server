package com.billsplitting.service;

import com.billsplitting.entity.Expense;
import com.billsplitting.entity.ExpenseGroup;
import com.billsplitting.entity.GroupMember;
import com.billsplitting.exception.ExpenseNotFoundException;
import com.billsplitting.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseGroupService expenseGroupService;
    private final GroupMemberService groupMemberService;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository,
                         ExpenseGroupService expenseGroupService,
                         GroupMemberService groupMemberService) {
        this.expenseRepository = expenseRepository;
        this.expenseGroupService = expenseGroupService;
        this.groupMemberService = groupMemberService;
    }

    public Expense addExpense(String groupName, String description, BigDecimal amount, String paidBy) {
        ExpenseGroup group = expenseGroupService.getGroupByName(groupName);
        GroupMember paidByMember = groupMemberService.getMemberByGroupNameAndMemberName(groupName, paidBy);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than 0");
        }
        
        Expense expense = new Expense(group, description, amount, paidByMember);
        return expenseRepository.save(expense);
    }

    public Expense updateExpense(Long expenseId, String description, BigDecimal amount, String paidBy) {
        Expense expense = getExpenseById(expenseId);
        
        if (description != null && !description.trim().isEmpty()) {
            expense.setDescription(description);
        }
        
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Expense amount must be greater than 0");
            }
            expense.setAmount(amount);
        }
        
        if (paidBy != null && !paidBy.trim().isEmpty()) {
            GroupMember paidByMember = groupMemberService.getMemberByGroupNameAndMemberName(
                expense.getGroup().getName(), paidBy);
            expense.setPaidByMember(paidByMember);
        }
        
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long expenseId) {
        Expense expense = getExpenseById(expenseId);
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(Long expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense with ID " + expenseId + " not found"));
    }

    @Transactional(readOnly = true)
    public Expense getExpenseByIdWithSplits(Long expenseId) {
        return expenseRepository.findByIdWithSplits(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense with ID " + expenseId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesByGroup(String groupName) {
        return expenseRepository.findByGroupNameOrderByCreatedAtDesc(groupName);
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesByGroupAndDateRange(String groupName, LocalDateTime startDate, LocalDateTime endDate) {
        ExpenseGroup group = expenseGroupService.getGroupByName(groupName);
        return expenseRepository.findByGroupIdAndDateRange(group.getId(), startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesByGroup(String groupName) {
        BigDecimal total = expenseRepository.getTotalExpensesByGroupName(groupName);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesPaidByMember(String groupName, String memberName) {
        GroupMember member = groupMemberService.getMemberByGroupNameAndMemberName(groupName, memberName);
        return expenseRepository.findByPaidByMemberId(member.getId());
    }
}