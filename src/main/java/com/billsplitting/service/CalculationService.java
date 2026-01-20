package com.billsplitting.service;

import com.billsplitting.dto.MemberBalance;
import com.billsplitting.dto.Settlement;
import com.billsplitting.entity.Expense;
import com.billsplitting.entity.ExpenseSplit;
import com.billsplitting.entity.GroupMember;
import com.billsplitting.repository.ExpenseSplitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CalculationService {

    private final ExpenseService expenseService;
    private final GroupMemberService groupMemberService;
    private final ExpenseSplitRepository expenseSplitRepository;

    @Autowired
    public CalculationService(ExpenseService expenseService,
                             GroupMemberService groupMemberService,
                             ExpenseSplitRepository expenseSplitRepository) {
        this.expenseService = expenseService;
        this.groupMemberService = groupMemberService;
        this.expenseSplitRepository = expenseSplitRepository;
    }

    public Map<String, MemberBalance> calculateGroupTotals(String groupName) {
        List<GroupMember> members = groupMemberService.listMembers(groupName);
        Map<String, MemberBalance> balances = new HashMap<>();

        for (GroupMember member : members) {
            // Calculate total paid by this member
            BigDecimal totalPaid = expenseService.getExpensesPaidByMember(groupName, member.getMemberName())
                    .stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate total owed by this member
            BigDecimal totalOwed = expenseSplitRepository.findByGroupNameAndMemberName(groupName, member.getMemberName())
                    .stream()
                    .map(ExpenseSplit::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            MemberBalance balance = new MemberBalance(member.getMemberName(), totalPaid, totalOwed);
            balances.put(member.getMemberName(), balance);
        }

        return balances;
    }

    public MemberBalance calculateMemberBalance(String groupName, String memberName) {
        // Verify member exists
        groupMemberService.getMemberByGroupNameAndMemberName(groupName, memberName);

        // Calculate total paid
        BigDecimal totalPaid = expenseService.getExpensesPaidByMember(groupName, memberName)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total owed
        BigDecimal totalOwed = expenseSplitRepository.findByGroupNameAndMemberName(groupName, memberName)
                .stream()
                .map(ExpenseSplit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MemberBalance(memberName, totalPaid, totalOwed);
    }

    public List<Settlement> generateSettlementPlan(String groupName) {
        Map<String, MemberBalance> balances = calculateGroupTotals(groupName);
        
        // Separate creditors (positive balance) and debtors (negative balance)
        List<MemberBalance> creditors = balances.values().stream()
                .filter(MemberBalance::isCreditor)
                .sorted((a, b) -> b.getNetBalance().compareTo(a.getNetBalance())) // Sort by balance descending
                .collect(Collectors.toList());

        List<MemberBalance> debtors = balances.values().stream()
                .filter(MemberBalance::isDebtor)
                .sorted((a, b) -> a.getNetBalance().compareTo(b.getNetBalance())) // Sort by balance ascending (most negative first)
                .collect(Collectors.toList());

        List<Settlement> settlements = new ArrayList<>();

        // Create working copies to avoid modifying original balances
        Queue<MemberBalance> creditorQueue = new LinkedList<>();
        Queue<MemberBalance> debtorQueue = new LinkedList<>();

        for (MemberBalance creditor : creditors) {
            creditorQueue.offer(new MemberBalance(creditor.getMemberName(), 
                creditor.getTotalPaid(), creditor.getTotalOwed()));
        }

        for (MemberBalance debtor : debtors) {
            debtorQueue.offer(new MemberBalance(debtor.getMemberName(), 
                debtor.getTotalPaid(), debtor.getTotalOwed()));
        }

        // Match creditors with debtors to minimize transactions
        while (!creditorQueue.isEmpty() && !debtorQueue.isEmpty()) {
            MemberBalance creditor = creditorQueue.poll();
            MemberBalance debtor = debtorQueue.poll();

            BigDecimal creditorAmount = creditor.getNetBalance();
            BigDecimal debtorAmount = debtor.getNetBalance().abs(); // Make positive

            BigDecimal settlementAmount = creditorAmount.min(debtorAmount);
            settlements.add(new Settlement(debtor.getMemberName(), creditor.getMemberName(), settlementAmount));

            // Update balances
            BigDecimal newCreditorBalance = creditorAmount.subtract(settlementAmount);
            BigDecimal newDebtorBalance = debtorAmount.subtract(settlementAmount);

            // Re-queue if there's remaining balance
            if (newCreditorBalance.compareTo(BigDecimal.ZERO) > 0) {
                creditor.setNetBalance(newCreditorBalance);
                creditorQueue.offer(creditor);
            }

            if (newDebtorBalance.compareTo(BigDecimal.ZERO) > 0) {
                debtor.setNetBalance(newDebtorBalance.negate()); // Make negative again
                debtorQueue.offer(debtor);
            }
        }

        return settlements;
    }

    public BigDecimal getTotalGroupExpenses(String groupName) {
        return expenseService.getTotalExpensesByGroup(groupName);
    }

    public Map<String, BigDecimal> getMemberPaymentSummary(String groupName) {
        List<GroupMember> members = groupMemberService.listMembers(groupName);
        Map<String, BigDecimal> paymentSummary = new HashMap<>();

        for (GroupMember member : members) {
            BigDecimal totalPaid = expenseService.getExpensesPaidByMember(groupName, member.getMemberName())
                    .stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            paymentSummary.put(member.getMemberName(), totalPaid);
        }

        return paymentSummary;
    }
}