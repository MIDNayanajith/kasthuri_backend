package com.enterprise.bms.enterprise_bms.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_own_vehicles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OwnVehiclesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reg_number", nullable = false, unique = true)
    private String regNumber;

    private String type;

    private BigDecimal capacity;

    @Column(name = "current_mileage")
    private BigDecimal currentMileage = BigDecimal.ZERO;

    private String status = "Available"; // Available, Busy, Maintenance

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    private DriversEntity assignedDriver;

    @Column(name = "is_delete")
    private Boolean isDelete = false;

    @Column(name = "created_at")
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

