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
public class TireMaintenanceDTO {

    private Long id;
    private Long vehicleId;   // Reference to OwnVehiclesEntity (ID only)
    private String vehicleRegNumber;
    private String position;
    private LocalDate date;
    private String tireBrand;
    private String tireSize;
    private String serialNumber;
    private String description;
    private BigDecimal mileage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Boolean isDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}