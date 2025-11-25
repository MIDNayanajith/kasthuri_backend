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
@Table(name = "tbl_transports")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name", nullable = false, length = 150)
    private String clientName;

    @Column(length = 255)
    private String description;

    @Column(name = "starting_point", nullable = false, length = 200)
    private String startingPoint;

    @Column(name = "destination", nullable = false, length = 200)
    private String destination;

    @Column(name = "loading_date", nullable = false)
    private LocalDate loadingDate;

    @Column(name = "unloading_date")
    private LocalDate unloadingDate;

    // ------------------- Relationships -------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "own_vehicle_id",
            foreignKey = @ForeignKey(name = "FK_transport_own_vehicle")
    )
    private OwnVehiclesEntity ownVehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ext_hire_id",
            foreignKey = @ForeignKey(name = "FK_transport_external_vehicle")
    )
    private ExVehiclesEntity externalVehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "internal_driver_id",
            foreignKey = @ForeignKey(name = "FK_transport_internal_driver")
    )
    private DriversEntity internalDriver;

    // ------------------- Financial Fields -------------------

    @Column(name = "distance_km", precision = 8, scale = 2, nullable = false)
    private BigDecimal distanceKm = BigDecimal.ZERO;

    @Column(name = "agreed_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal agreedAmount;

    @Column(name = "advance_received", precision = 12, scale = 2)
    private BigDecimal advanceReceived = BigDecimal.ZERO;

    @Column(name = "balance_received", precision = 12, scale = 2)
    private BigDecimal balanceReceived = BigDecimal.ZERO;

    @Column(name = "held_up", precision = 12, scale = 2)
    private BigDecimal heldUp = BigDecimal.ZERO;

    /**
     * Payment Status:
     * 1 = Pending
     * 2 = Advance Paid
     * 3 = Fully Paid
     */
    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus = 1;

    /**
     * Trip Status:
     * 1 = Pending
     * 2 = Completed
     * 3 = Cancelled
     */
    @Column(name = "trip_status", nullable = false)
    private Integer tripStatus = 1;

    @Column(name = "is_delete")
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ------------------- Auto Timestamp -------------------

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }    }
