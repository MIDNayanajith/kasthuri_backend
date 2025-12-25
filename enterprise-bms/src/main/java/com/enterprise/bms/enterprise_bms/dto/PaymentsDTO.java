package com.enterprise.bms.enterprise_bms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentsDTO {
    private Long id;
    private String recipientType; // "Driver" or "User"
    private Long recipientId;
    private Integer periodMonth;
    private Integer periodYear;
    private BigDecimal baseAmount;
    private BigDecimal tripBonus;
    private BigDecimal deductions;
    private BigDecimal advancesDeducted;
    private BigDecimal netPay;
    private LocalDate paymentDate;
    private String status; // "Pending", "Paid"
    private String notes;
    private Long createdBy;
    private Boolean isDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}