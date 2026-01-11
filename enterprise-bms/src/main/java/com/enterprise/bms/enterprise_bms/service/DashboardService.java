package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.DashboardDTO;
import com.enterprise.bms.enterprise_bms.dto.MonthlyData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TransportService transportService;
    private final FuelService fuelService;
    private final MaintenanceService maintenanceService;
    private final TireMaintenanceService tireMaintenanceService;
    private final PaymentService paymentService;
    private final ExVehicleService exVehicleService;

    public DashboardDTO getDashboardData() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        YearMonth currentYM = YearMonth.of(currentYear, currentMonth);
        LocalDate monthStart = currentYM.atDay(1);
        LocalDate monthEnd = currentYM.atEndOfMonth();

        // Calculate current month totals
        BigDecimal income = transportService.getTotalIncomeForPeriod(monthStart, monthEnd);
        BigDecimal fuelExp = fuelService.getTotalFuelCostForPeriod(monthStart, monthEnd);
        BigDecimal maintExp = maintenanceService.getTotalMaintenanceCostForPeriod(monthStart, monthEnd);
        BigDecimal tireExp = tireMaintenanceService.getTotalTireMaintenanceCostForPeriod(monthStart, monthEnd);
        BigDecimal paymentExp = paymentService.getTotalPaymentsForPeriod(currentYear, currentMonth);
        BigDecimal exVehExp = exVehicleService.getTotalHireCostForPeriod(monthStart, monthEnd);

        BigDecimal totalExpense = fuelExp.add(maintExp).add(tireExp).add(paymentExp).add(exVehExp);
        BigDecimal totalMaintenance = maintExp.add(tireExp);
        BigDecimal netProfit = income.subtract(totalExpense);

        // Monthly data for charts
        List<MonthlyData> monthlyIncomes = new ArrayList<>();
        List<MonthlyData> monthlyExpenses = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM"); // e.g., "Jan"

        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(currentYear, m);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            String monthName = ym.format(monthFormatter);

            // Income for month
            BigDecimal monthlyIncome = transportService.getTotalIncomeForPeriod(start, end);

            // Expenses for month
            BigDecimal mFuel = fuelService.getTotalFuelCostForPeriod(start, end);
            BigDecimal mMaint = maintenanceService.getTotalMaintenanceCostForPeriod(start, end);
            BigDecimal mTire = tireMaintenanceService.getTotalTireMaintenanceCostForPeriod(start, end);
            BigDecimal mPayment = paymentService.getTotalPaymentsForPeriod(currentYear, m);
            BigDecimal mExVeh = exVehicleService.getTotalHireCostForPeriod(start, end);
            BigDecimal monthlyExpense = mFuel.add(mMaint).add(mTire).add(mPayment).add(mExVeh);

            monthlyIncomes.add(MonthlyData.builder().month(monthName).amount(monthlyIncome).build());
            monthlyExpenses.add(MonthlyData.builder().month(monthName).amount(monthlyExpense).build());
        }

        return DashboardDTO.builder()
                .totalIncome(income != null ? income : BigDecimal.ZERO)
                .totalExpense(totalExpense != null ? totalExpense : BigDecimal.ZERO)
                .totalMaintenanceCost(totalMaintenance != null ? totalMaintenance : BigDecimal.ZERO)
                .netProfit(netProfit != null ? netProfit : BigDecimal.ZERO)
                .monthlyIncomes(monthlyIncomes)
                .monthlyExpenses(monthlyExpenses)
                .build();
    }
}