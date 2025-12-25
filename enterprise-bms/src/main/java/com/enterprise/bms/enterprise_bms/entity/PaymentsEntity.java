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
@Table(name = "tbl_payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientType; // "Driver" or "User"

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private Integer periodMonth;

    @Column(nullable = false)
    private Integer periodYear;

    @Column(precision = 12, scale = 2)
    private BigDecimal baseAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal tripBonus;

    @Column(precision = 12, scale = 2)
    private BigDecimal deductions;

    @Column(precision = 12, scale = 2)
    private BigDecimal advancesDeducted;

    @Column(precision = 12, scale = 2)
    private BigDecimal netPay;

    private LocalDate paymentDate;

    @Column(nullable = false)
    private String status = "Pending"; // "Pending", "Paid"

    private String notes;

    private Long createdBy;

    private Boolean isDelete = false;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}