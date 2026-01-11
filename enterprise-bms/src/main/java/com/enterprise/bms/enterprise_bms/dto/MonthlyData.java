package com.enterprise.bms.enterprise_bms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyData {
    private String month; // e.g., "Jan"
    private BigDecimal amount;
}