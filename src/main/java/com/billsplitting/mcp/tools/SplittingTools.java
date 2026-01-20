package com.billsplitting.mcp.tools;

import com.billsplitting.entity.ExpenseSplit;
import com.billsplitting.service.ExpenseSplitService;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SplittingTools {

    private final ExpenseSplitService expenseSplitService;

    public SplittingTools(ExpenseSplitService expenseSplitService) {
        this.expenseSplitService = expenseSplitService;
    }

    @McpTool(name = "split_expense_equally", description = "Split an expense equally among all group members")
    public SplitExpenseResponse splitExpenseEqually(
            @McpArg(name = "expenseId", description = "ID of the expense to split", required = true) Long expenseId) {
        expenseSplitService.splitEqually(expenseId);
        List<ExpenseSplit> splits = expenseSplitService.getSplitsByExpense(expenseId);
        
        List<SplitDetail> splitDetails = splits.stream()
                .map(split -> new SplitDetail(split.getMember().getMemberName(), split.getAmount()))
                .collect(Collectors.toList());
        
        return new SplitExpenseResponse(expenseId, "EQUAL", splitDetails, "Expense split equally among members");
    }

    @McpTool(name = "split_expense_by_amount", description = "Split an expense by custom amounts for each member")
    public SplitExpenseResponse splitExpenseByAmount(
            @McpArg(name = "expenseId", description = "ID of the expense to split", required = true) Long expenseId,
            @McpArg(name = "memberAmounts", description = "Map of member names to their respective amounts in INR", required = true) Map<String, BigDecimal> memberAmounts) {
        expenseSplitService.splitByAmount(expenseId, memberAmounts);
        List<ExpenseSplit> splits = expenseSplitService.getSplitsByExpense(expenseId);
        
        List<SplitDetail> splitDetails = splits.stream()
                .map(split -> new SplitDetail(split.getMember().getMemberName(), split.getAmount()))
                .collect(Collectors.toList());
        
        return new SplitExpenseResponse(expenseId, "AMOUNT", splitDetails, "Expense split by custom amounts");
    }

    @McpTool(name = "split_expense_by_percentage", description = "Split an expense by percentage shares for each member")
    public SplitExpenseResponse splitExpenseByPercentage(
            @McpArg(name = "expenseId", description = "ID of the expense to split", required = true) Long expenseId,
            @McpArg(name = "memberPercentages", description = "Map of member names to their percentage shares (0-100)", required = true) Map<String, BigDecimal> memberPercentages) {
        expenseSplitService.splitByPercentage(expenseId, memberPercentages);
        List<ExpenseSplit> splits = expenseSplitService.getSplitsByExpense(expenseId);
        
        List<SplitDetail> splitDetails = splits.stream()
                .map(split -> new SplitDetail(split.getMember().getMemberName(), split.getAmount(), split.getPercentage()))
                .collect(Collectors.toList());
        
        return new SplitExpenseResponse(expenseId, "PERCENTAGE", splitDetails, "Expense split by percentages");
    }

    public record SplitExpenseResponse(
            Long expenseId,
            String splitType,
            List<SplitDetail> splits,
            String message
    ) {}

    public record SplitDetail(
            String memberName,
            BigDecimal amount,
            BigDecimal percentage
    ) {
        // Constructor for splits without percentage
        public SplitDetail(String memberName, BigDecimal amount) {
            this(memberName, amount, null);
        }
    }
}