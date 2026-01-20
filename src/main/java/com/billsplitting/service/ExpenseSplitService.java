package com.billsplitting.service;

import com.billsplitting.entity.Expense;
import com.billsplitting.entity.ExpenseSplit;
import com.billsplitting.entity.GroupMember;
import com.billsplitting.entity.SplitType;
import com.billsplitting.exception.InvalidSplitException;
import com.billsplitting.repository.ExpenseSplitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ExpenseSplitService {

    private final ExpenseSplitRepository expenseSplitRepository;
    private final ExpenseService expenseService;
    private final GroupMemberService groupMemberService;

    @Autowired
    public ExpenseSplitService(ExpenseSplitRepository expenseSplitRepository,
                              ExpenseService expenseService,
                              GroupMemberService groupMemberService) {
        this.expenseSplitRepository = expenseSplitRepository;
        this.expenseService = expenseService;
        this.groupMemberService = groupMemberService;
    }

    public void splitEqually(Long expenseId) {
        Expense expense = expenseService.getExpenseById(expenseId);
        List<GroupMember> members = groupMemberService.listMembers(expense.getGroup().getName());
        
        if (members.isEmpty()) {
            throw new InvalidSplitException("Cannot split expense: no members in group");
        }
        
        // Clear existing splits
        expenseSplitRepository.deleteByExpenseId(expenseId);
        
        BigDecimal totalAmount = expense.getAmount();
        int memberCount = members.size();
        
        // Calculate base amount per member (rounded down to nearest paisa)
        BigDecimal baseAmount = totalAmount.divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.DOWN);
        
        // Calculate remainder in paisa
        BigDecimal totalBaseAmount = baseAmount.multiply(BigDecimal.valueOf(memberCount));
        BigDecimal remainder = totalAmount.subtract(totalBaseAmount);
        int remainderPaisa = remainder.multiply(BigDecimal.valueOf(100)).intValue();
        
        // Create splits
        for (int i = 0; i < members.size(); i++) {
            BigDecimal memberAmount = baseAmount;
            
            // Distribute remainder (1 paisa each to first few members)
            if (i < remainderPaisa) {
                memberAmount = memberAmount.add(BigDecimal.valueOf(0.01));
            }
            
            ExpenseSplit split = new ExpenseSplit(expense, members.get(i), memberAmount);
            expenseSplitRepository.save(split);
        }
        
        // Update expense split type
        expense.setSplitType(SplitType.EQUAL);
        expenseService.updateExpense(expenseId, null, null, null);
    }

    public void splitByAmount(Long expenseId, Map<String, BigDecimal> memberAmounts) {
        Expense expense = expenseService.getExpenseById(expenseId);
        String groupName = expense.getGroup().getName();
        
        // Validate that amounts sum to expense total
        BigDecimal totalSplitAmount = memberAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalSplitAmount.compareTo(expense.getAmount()) != 0) {
            throw new InvalidSplitException(
                String.format("Split amounts (%.2f) do not equal expense amount (%.2f)", 
                    totalSplitAmount, expense.getAmount()));
        }
        
        // Validate all members exist
        for (String memberName : memberAmounts.keySet()) {
            if (!groupMemberService.memberExists(groupName, memberName)) {
                throw new InvalidSplitException("Member '" + memberName + "' not found in group");
            }
        }
        
        // Clear existing splits
        expenseSplitRepository.deleteByExpenseId(expenseId);
        
        // Create new splits
        for (Map.Entry<String, BigDecimal> entry : memberAmounts.entrySet()) {
            String memberName = entry.getKey();
            BigDecimal amount = entry.getValue();
            
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidSplitException("Split amount cannot be negative for member: " + memberName);
            }
            
            GroupMember member = groupMemberService.getMemberByGroupNameAndMemberName(groupName, memberName);
            ExpenseSplit split = new ExpenseSplit(expense, member, amount);
            expenseSplitRepository.save(split);
        }
        
        // Update expense split type
        expense.setSplitType(SplitType.AMOUNT);
    }

    public void splitByPercentage(Long expenseId, Map<String, BigDecimal> memberPercentages) {
        Expense expense = expenseService.getExpenseById(expenseId);
        String groupName = expense.getGroup().getName();
        
        // Validate that percentages sum to 100%
        BigDecimal totalPercentage = memberPercentages.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new InvalidSplitException(
                String.format("Percentages must sum to 100%%, got %.2f%%", totalPercentage));
        }
        
        // Validate all members exist and percentages are valid
        for (Map.Entry<String, BigDecimal> entry : memberPercentages.entrySet()) {
            String memberName = entry.getKey();
            BigDecimal percentage = entry.getValue();
            
            if (!groupMemberService.memberExists(groupName, memberName)) {
                throw new InvalidSplitException("Member '" + memberName + "' not found in group");
            }
            
            if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new InvalidSplitException("Invalid percentage for member " + memberName + ": " + percentage);
            }
        }
        
        // Clear existing splits
        expenseSplitRepository.deleteByExpenseId(expenseId);
        
        // Calculate amounts from percentages
        BigDecimal totalAmount = expense.getAmount();
        BigDecimal totalCalculatedAmount = BigDecimal.ZERO;
        
        // First pass: calculate amounts for all but the last member
        String lastMemberName = null;
        for (Map.Entry<String, BigDecimal> entry : memberPercentages.entrySet()) {
            lastMemberName = entry.getKey();
        }
        
        for (Map.Entry<String, BigDecimal> entry : memberPercentages.entrySet()) {
            String memberName = entry.getKey();
            BigDecimal percentage = entry.getValue();
            
            GroupMember member = groupMemberService.getMemberByGroupNameAndMemberName(groupName, memberName);
            
            BigDecimal amount;
            if (memberName.equals(lastMemberName)) {
                // Last member gets the remainder to ensure exact total
                amount = totalAmount.subtract(totalCalculatedAmount);
            } else {
                amount = totalAmount.multiply(percentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalCalculatedAmount = totalCalculatedAmount.add(amount);
            }
            
            ExpenseSplit split = new ExpenseSplit(expense, member, amount, percentage);
            expenseSplitRepository.save(split);
        }
        
        // Update expense split type
        expense.setSplitType(SplitType.PERCENTAGE);
    }

    @Transactional(readOnly = true)
    public List<ExpenseSplit> getSplitsByExpense(Long expenseId) {
        return expenseSplitRepository.findByExpenseId(expenseId);
    }

    @Transactional(readOnly = true)
    public List<ExpenseSplit> getSplitsByMember(String groupName, String memberName) {
        return expenseSplitRepository.findByGroupNameAndMemberName(groupName, memberName);
    }
}