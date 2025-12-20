// Updated TransportDTO.java (add new fields only; replace the entire class with this)
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
public class TransportDTO {
    private Long id;
    private String clientName;
    private String description;
    private String startingPoint;
    private String destination;
    private LocalDate loadingDate;
    private LocalDate unloadingDate;
    // Related entity IDs (for create/update operations)
    private Long ownVehicleId;
    private Long externalVehicleId;
    private Long internalDriverId;
    // Financial fields
    private BigDecimal distanceKm;
    private BigDecimal agreedAmount;
    private BigDecimal advanceReceived;
    private BigDecimal balanceReceived;
    private BigDecimal heldUp;
    // Status fields
    private Integer paymentStatus; // 1=Pending, 2=Advance Paid, 3=Fully Paid
    private Integer tripStatus; // 1=Pending, 2=Completed, 3=Cancelled
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // New fields
    private Long invoiceId;
    private String invoiceStatus;
}