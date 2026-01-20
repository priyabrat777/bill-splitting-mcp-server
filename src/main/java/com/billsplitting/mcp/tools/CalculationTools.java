package com.billsplitting.mcp.tools;

import com.billsplitting.dto.MemberBalance;
import com.billsplitting.dto.Settlement;
import com.billsplitting.service.CalculationService;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CalculationTools {

    private final CalculationService calculationService;

    public CalculationTools(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @McpTool(name = "calculate_group_totals", description = "Calculate total expenses and member balances for a group")
    public GroupTotalsResponse calculateGroupTotals(
            @McpArg(name = "groupName", description = "Name of the expense group", required = true) String groupName) {
        Map<String, MemberBalance> balances = calculationService.calculateGroupTotals(groupName);
        BigDecimal totalExpenses = calculationService.getTotalGroupExpenses(groupName);
        
        List<MemberBalanceDetail> memberBalances = balances.values().stream()
                .map(balance -> new MemberBalanceDetail(balance.getMemberName(), balance.getTotalPaid(),
                        balance.getTotalOwed(), balance.getNetBalance()))
                .collect(Collectors.toList());
        
        return new GroupTotalsResponse(groupName, totalExpenses, memberBalances);
    }

    @McpTool(name = "get_member_balance", description = "Get balance details for a specific member")
    public MemberBalanceDetail getMemberBalance(
            @McpArg(name = "groupName", description = "Name of the expense group", required = true) String groupName,
            @McpArg(name = "memberName", description = "Name of the member", required = true) String memberName) {
        MemberBalance balance = calculationService.calculateMemberBalance(groupName, memberName);
        return new MemberBalanceDetail(balance.getMemberName(), balance.getTotalPaid(),
                balance.getTotalOwed(), balance.getNetBalance());
    }

    @McpTool(name = "generate_settlement_summary", description = "Generate a final settlement summary with payment recommendations")
    public SettlementSummaryResponse generateSettlementSummary(
            @McpArg(name = "groupName", description = "Name of the expense group", required = true) String groupName) {
        List<Settlement> settlements = calculationService.generateSettlementPlan(groupName);
        BigDecimal totalExpenses = calculationService.getTotalGroupExpenses(groupName);
        
        List<SettlementDetail> settlementDetails = settlements.stream()
                .map(settlement -> new SettlementDetail(settlement.getFromMember(), 
                        settlement.getToMember(), settlement.getAmount()))
                .collect(Collectors.toList());
        
        return new SettlementSummaryResponse(groupName, totalExpenses, settlementDetails,
                "Settlement plan generated with " + settlements.size() + " transactions");
    }

    @McpTool(name = "get_expense_history", description = "Get expense history for a group or all groups")
    public ExpenseHistoryResponse getExpenseHistory(
            @McpArg(name = "groupName", description = "Name of the expense group", required = true) String groupName) {
        // This would integrate with ExpenseService to get detailed expense history
        // For now, returning a placeholder structure
        return new ExpenseHistoryResponse(groupName, "Expense history retrieved");
    }

    public record GroupTotalsResponse(
            String groupName,
            BigDecimal totalExpenses,
            List<MemberBalanceDetail> memberBalances
    ) {}

    public record MemberBalanceDetail(
            String memberName,
            BigDecimal totalPaid,
            BigDecimal totalOwed,
            BigDecimal netBalance
    ) {}

    public record SettlementSummaryResponse(
            String groupName,
            BigDecimal totalExpenses,
            List<SettlementDetail> settlements,
            String message
    ) {}

    public record SettlementDetail(
            String fromMember,
            String toMember,
            BigDecimal amount
    ) {}

    public record ExpenseHistoryResponse(
            String groupName,
            String message
    ) {}
}