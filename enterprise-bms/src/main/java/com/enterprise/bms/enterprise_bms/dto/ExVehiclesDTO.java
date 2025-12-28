package com.enterprise.bms.enterprise_bms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExVehiclesDTO {

    private Long id;
    private String regNumber;
    private String ownerName;
    private String ownerContact;
    private BigDecimal hireRate;
    private BigDecimal vehicleUsage;

    // Payment fields
    private BigDecimal advancePaid;      // Initial advance
    private BigDecimal totalPaid;        // Total paid so far
    private BigDecimal balance;          // Remaining to pay

    private Integer paymentStatus;
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For making additional payments
    private BigDecimal newPayment;       // For recording additional payments
}