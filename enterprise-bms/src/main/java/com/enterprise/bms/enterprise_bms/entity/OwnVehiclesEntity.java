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
    private Long id;   // PK

    @Column(nullable = false, unique = true, length = 20)
    private String regNumber;

    @Column(length = 50)
    private String type;   // e.g., "Container"

    @Column(precision = 5, scale = 2)
    private BigDecimal capacity;   // e.g., 20.00 tons

    @Column(precision = 10, scale = 2)
    private BigDecimal currentMileage = BigDecimal.ZERO;

    @Column(length = 100)
    private String status = "Available";
    // ('Available', 'Busy', 'Maintenance')

    // FK: assigned_driver_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    private DriversEntity assignedDriver;

    private Boolean isDelete = false;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

