package com.billsplitting.mcp.tools;

import com.billsplitting.entity.Expense;
import com.billsplitting.service.ExpenseService;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExpenseTools {

    private final ExpenseService expenseService;

    public ExpenseTools(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @McpTool(name = "add_expense", description = "Add an expense to a group")
    public AddExpenseResponse addExpense(
            @McpArg(name = "groupName", description = "Name of the expense group", required = true) String groupName,
            @McpArg(name = "description", description = "Description of the expense", required = true) String description,
            @McpArg(name = "amount", description = "Amount of the expense in INR", required = true) BigDecimal amount,
            @McpArg(name = "paidBy", description = "Name of the member who paid for the expense", required = true) String paidBy) {
        Expense expense = expenseService.addExpense(groupName, description, amount, paidBy);
        return new AddExpenseResponse(expense.getId(), expense.getDescription(), expense.getAmount(),
                expense.getPaidByMember().getMemberName(), expense.getGroup().getName(), 
                expense.getCreatedAt().toString());
    }

    @McpTool(name = "update_expense", description = "Update an existing expense")
    public UpdateExpenseResponse updateExpense(
            @McpArg(name = "expenseId", description = "ID of the expense to update", required = true) Long expenseId,
            @McpArg(name = "description", description = "New description of the expense", required = false) String description,
            @McpArg(name = "amount", description = "New amount of the expense in INR", required = false) BigDecimal amount,
            @McpArg(name = "paidBy", description = "New name of the member who paid", required = false) String paidBy) {
        Expense expense = expenseService.updateExpense(expenseId, description, amount, paidBy);
        return new UpdateExpenseResponse(expense.getId(), expense.getDescription(), expense.getAmount(),
                expense.getPaidByMember().getMemberName(), expense.getUpdatedAt().toString());
    }

    @McpTool(name = "delete_expense", description = "Delete an expense")
    public DeleteExpenseResponse deleteExpense(
            @McpArg(name = "expenseId", description = "ID of the expense to delete", required = true) Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return new DeleteExpenseResponse(expenseId, "Expense deleted successfully");
    }

    @McpTool(name = "list_expenses", description = "List all expenses for a group")
    public List<ExpenseSummary> listExpenses(
            @McpArg(name = "groupName", description = "Name of the expense group", required = true) String groupName) {
        return expenseService.getExpensesByGroup(groupName).stream()
                .map(expense -> new ExpenseSummary(expense.getId(), expense.getDescription(), 
                        expense.getAmount(), expense.getPaidByMember().getMemberName(),
                        expense.getSplitType().toString(), expense.getCreatedAt().toString()))
                .collect(Collectors.toList());
    }

    public record AddExpenseResponse(
            Long id,
            String description,
            BigDecimal amount,
            String paidBy,
            String groupName,
            String createdAt
    ) {}

    public record UpdateExpenseResponse(
            Long id,
            String description,
            BigDecimal amount,
            String paidBy,
            String updatedAt
    ) {}

    public record DeleteExpenseResponse(
            Long expenseId,
            String message
    ) {}

    public record ExpenseSummary(
            Long id,
            String description,
            BigDecimal amount,
            String paidBy,
            String splitType,
            String createdAt
    ) {}
}