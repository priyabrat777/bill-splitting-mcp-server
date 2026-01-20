package com.billsplitting.service;

import com.billsplitting.entity.*;
import com.billsplitting.repository.ExpenseSplitRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseSplitServicePropertyTest {

    @Mock
    private ExpenseSplitRepository expenseSplitRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private GroupMemberService groupMemberService;

    @InjectMocks
    private ExpenseSplitService expenseSplitService;

    @BeforeEach
    void setUp() {
        // Mock the deleteByExpenseId call
        doNothing().when(expenseSplitRepository).deleteByExpenseId(anyLong());
    }

    /**
     * **Validates: Requirements 1.1**
     * Property 1.1: Split Conservation
     * For any expense split, the sum of all member splits must equal the original expense amount.
     */
    @Property
    void splitConservation(@ForAll @BigRange(min = "0.01", max = "10000.00") BigDecimal expenseAmount,
                          @ForAll @IntRange(min = 1, max = 20) int memberCount) {
        
        // Given
        ExpenseGroup group = new ExpenseGroup("Test Group", "Test Description");
        group.setId(1L);
        
        List<GroupMember> members = new ArrayList<>();
        for (int i = 0; i < memberCount; i++) {
            GroupMember member = new GroupMember(group, "Member" + i);
            member.setId((long) (i + 1));
            members.add(member);
        }
        
        GroupMember paidByMember = members.get(0);
        Expense expense = new Expense(group, "Test Expense", expenseAmount, paidByMember);
        expense.setId(1L);
        
        // Mock the service calls
        when(expenseService.getExpenseById(1L)).thenReturn(expense);
        when(groupMemberService.listMembers("Test Group")).thenReturn(members);
        
        // Capture the splits that would be saved
        List<ExpenseSplit> capturedSplits = new ArrayList<>();
        when(expenseSplitRepository.save(any(ExpenseSplit.class))).thenAnswer(invocation -> {
            ExpenseSplit split = invocation.getArgument(0);
            capturedSplits.add(split);
            return split;
        });
        
        // When
        expenseSplitService.splitEqually(1L);
        
        // Then - Property 1.1: Split Conservation
        BigDecimal totalSplitAmount = capturedSplits.stream()
                .map(ExpenseSplit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        assertEquals(0, expenseAmount.compareTo(totalSplitAmount), 
            "Sum of splits must equal original expense amount");
        
        // Verify all members got a split
        assertEquals(memberCount, capturedSplits.size(), 
            "Number of splits must equal number of members");
    }

    /**
     * **Validates: Requirements 1.2**
     * Property 1.2: Equal Split Fairness
     * For equal splits, the maximum difference between any two member amounts should not exceed 0.01 INR (1 paisa).
     */
    @Property
    void equalSplitFairness(@ForAll @BigRange(min = "0.01", max = "10000.00") BigDecimal expenseAmount,
                           @ForAll @IntRange(min = 2, max = 20) int memberCount) {
        
        // Given
        ExpenseGroup group = new ExpenseGroup("Test Group", "Test Description");
        group.setId(1L);
        
        List<GroupMember> members = new ArrayList<>();
        for (int i = 0; i < memberCount; i++) {
            GroupMember member = new GroupMember(group, "Member" + i);
            member.setId((long) (i + 1));
            members.add(member);
        }
        
        GroupMember paidByMember = members.get(0);
        Expense expense = new Expense(group, "Test Expense", expenseAmount, paidByMember);
        expense.setId(1L);
        
        // Mock the service calls
        when(expenseService.getExpenseById(1L)).thenReturn(expense);
        when(groupMemberService.listMembers("Test Group")).thenReturn(members);
        
        // Capture the splits that would be saved
        List<ExpenseSplit> capturedSplits = new ArrayList<>();
        when(expenseSplitRepository.save(any(ExpenseSplit.class))).thenAnswer(invocation -> {
            ExpenseSplit split = invocation.getArgument(0);
            capturedSplits.add(split);
            return split;
        });
        
        // When
        expenseSplitService.splitEqually(1L);
        
        // Then - Property 1.2: Equal Split Fairness
        BigDecimal minAmount = capturedSplits.stream()
                .map(ExpenseSplit::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal maxAmount = capturedSplits.stream()
                .map(ExpenseSplit::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal difference = maxAmount.subtract(minAmount);
        BigDecimal maxAllowedDifference = BigDecimal.valueOf(0.01);
        
        assertTrue(difference.compareTo(maxAllowedDifference) <= 0,
            String.format("Maximum difference between splits (%.2f) should not exceed 0.01 INR", difference));
    }

    /**
     * **Validates: Requirements 1.3**
     * Property 1.3: Percentage Split Accuracy
     * For percentage-based splits, each member's amount should be within 0.01 INR of their percentage share.
     */
    @Property
    void percentageSplitAccuracy(@ForAll @BigRange(min = "1.00", max = "1000.00") BigDecimal expenseAmount) {
        
        // Given - Create a simple 50-50 split scenario
        ExpenseGroup group = new ExpenseGroup("Test Group", "Test Description");
        group.setId(1L);
        
        GroupMember member1 = new GroupMember(group, "Member1");
        member1.setId(1L);
        GroupMember member2 = new GroupMember(group, "Member2");
        member2.setId(2L);
        
        Expense expense = new Expense(group, "Test Expense", expenseAmount, member1);
        expense.setId(1L);
        
        // Mock the service calls
        when(expenseService.getExpenseById(1L)).thenReturn(expense);
        when(groupMemberService.memberExists("Test Group", "Member1")).thenReturn(true);
        when(groupMemberService.memberExists("Test Group", "Member2")).thenReturn(true);
        when(groupMemberService.getMemberByGroupNameAndMemberName("Test Group", "Member1")).thenReturn(member1);
        when(groupMemberService.getMemberByGroupNameAndMemberName("Test Group", "Member2")).thenReturn(member2);
        
        // Capture the splits that would be saved
        List<ExpenseSplit> capturedSplits = new ArrayList<>();
        when(expenseSplitRepository.save(any(ExpenseSplit.class))).thenAnswer(invocation -> {
            ExpenseSplit split = invocation.getArgument(0);
            capturedSplits.add(split);
            return split;
        });
        
        // When - Split 50-50
        Map<String, BigDecimal> percentages = Map.of(
            "Member1", BigDecimal.valueOf(50),
            "Member2", BigDecimal.valueOf(50)
        );
        
        expenseSplitService.splitByPercentage(1L, percentages);
        
        // Then - Property 1.3: Percentage Split Accuracy
        BigDecimal expectedAmountPerMember = expenseAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal tolerance = BigDecimal.valueOf(0.01);
        
        for (ExpenseSplit split : capturedSplits) {
            BigDecimal difference = split.getAmount().subtract(expectedAmountPerMember).abs();
            assertTrue(difference.compareTo(tolerance) <= 0,
                String.format("Member %s amount (%.2f) should be within 0.01 of expected (%.2f)", 
                    split.getMember().getMemberName(), split.getAmount(), expectedAmountPerMember));
        }
    }
}