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
public class OwnVehiclesDTO {

    private Long id;
    private String regNumber;
    private String type;
    private BigDecimal capacity;
    private BigDecimal currentMileage;
    private String status;
    private Long assignedDriverId;
    private Boolean isDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
