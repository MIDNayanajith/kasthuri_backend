package com.enterprise.bms.enterprise_bms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_external_vehicles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExVehiclesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reg_number", nullable = false)
    private String regNumber;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_contact", nullable = false)
    private String ownerContact;

    @Column(name = "hire_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal hireRate;

    @Column(name = "vehicle_usage", precision = 10, scale = 2)
    private BigDecimal vehicleUsage = BigDecimal.ZERO; // Distance in km or number of days

    @Column( precision = 12, scale = 2)
    private BigDecimal advance = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus = 1; // 1=Pending, 2=Advance Paid, 3=Fully Paid

    @Column(name = "is_delete")
    private Boolean isDelete = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

