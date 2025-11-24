package com.enterprise.bms.enterprise_bms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    private BigDecimal vehicleUsage; // Can be distance (km) or days

    private BigDecimal advance;

    private BigDecimal balance;

    private BigDecimal totalCost;

    private Integer paymentStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
