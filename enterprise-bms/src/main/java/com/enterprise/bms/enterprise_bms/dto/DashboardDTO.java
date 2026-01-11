package com.enterprise.bms.enterprise_bms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalMaintenanceCost;
    private BigDecimal netProfit;
    private List<MonthlyData> monthlyIncomes;
    private List<MonthlyData> monthlyExpenses;
}