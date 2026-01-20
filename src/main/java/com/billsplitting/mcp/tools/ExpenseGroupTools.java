package com.billsplitting.mcp.tools;

import com.billsplitting.entity.ExpenseGroup;
import com.billsplitting.service.ExpenseGroupService;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExpenseGroupTools {

    private final ExpenseGroupService expenseGroupService;

    public ExpenseGroupTools(ExpenseGroupService expenseGroupService) {
        this.expenseGroupService = expenseGroupService;
    }

    @McpTool(name = "create_expense_group", description = "Create a new expense group for organizing related expenses")
    public CreateExpenseGroupResponse createExpenseGroup(
            @McpArg(name = "name", description = "Name of the expense group", required = true) String name,
            @McpArg(name = "description", description = "Description of the expense group", required = false) String description) {
        ExpenseGroup group = expenseGroupService.createGroup(name, description);
        return new CreateExpenseGroupResponse(group.getId(), group.getName(), group.getDescription(), 
                group.getCreatedAt().toString());
    }

    @McpTool(name = "list_expense_groups", description = "List all expense groups")
    public List<ExpenseGroupSummary> listExpenseGroups() {
        return expenseGroupService.listGroups().stream()
                .map(group -> new ExpenseGroupSummary(group.getId(), group.getName(), 
                        group.getDescription(), group.getCreatedAt().toString()))
                .collect(Collectors.toList());
    }

    public record CreateExpenseGroupResponse(
            Long id,
            String name,
            String description,
            String createdAt
    ) {}

    public record ExpenseGroupSummary(
            Long id,
            String name,
            String description,
            String createdAt
    ) {}
}