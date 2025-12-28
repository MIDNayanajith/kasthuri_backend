package com.enterprise.bms.enterprise_bms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_fuel")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FuelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fuel_date", nullable = false)
    private LocalDate fuelDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "vehicle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_fuel_vehicle")
    )
    private OwnVehiclesEntity vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trip_id",
            foreignKey = @ForeignKey(name = "FK_fuel_transport")
    )
    private TransportEntity transport;

    @Column(name = "odometer_reading", precision = 10, scale = 2)
    private BigDecimal odometerReading;

    @Column(name = "fuel_quantity", precision = 8, scale = 2, nullable = false)
    private BigDecimal fuelQuantity;

    @Column(name = "total_cost", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalCost;

    @Column(length = 500)
    private String notes;

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