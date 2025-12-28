package com.enterprise.bms.enterprise_bms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_tire_maintenance")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TireMaintenanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private OwnVehiclesEntity ownVehicle;  // Reference to OwnVehiclesEntity

    @Column(nullable = false)
    private String position;  // e.g., "FRONT LEFT"

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column
    private String tireBrand;

    @Column
    private String tireSize;

    @Column
    private String serialNumber;

    @Column
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal mileage;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer quantity = 1;

    @Column(name = "unit_price", precision = 8, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}