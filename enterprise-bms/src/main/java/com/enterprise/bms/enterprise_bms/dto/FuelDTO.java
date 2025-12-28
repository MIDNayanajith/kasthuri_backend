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
public class FuelDTO {
    private Long id;
    private LocalDate fuelDate;
    private Long vehicleId;
    private Long tripId;
    private BigDecimal odometerReading;
    private BigDecimal fuelQuantity;
    private BigDecimal totalCost;
    private String notes;
    private Boolean isDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For display purposes
    private String vehicleRegNumber;
    private String clientName; // From transport
    private String tripDescription;
}