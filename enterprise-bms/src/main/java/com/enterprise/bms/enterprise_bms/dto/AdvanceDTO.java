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
public class AdvanceDTO {
    private Long id;
    private String recipientType; // "Driver" or "User"
    private Long recipientId;
    private BigDecimal amount;
    private LocalDate advanceDate;
    private String notes;
    private Long createdBy;
    private String status; // "Pending", "Deducted", "Partial"
    private Long deductedInPaymentId;
    private Boolean isDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}