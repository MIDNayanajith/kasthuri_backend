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
@Table(name = "tbl_advances")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientType; // "Driver" or "User"

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate advanceDate;

    private String notes;

    private Long createdBy;

    /**
     * Status: "Pending" (not yet deducted), "Deducted" (fully deducted in a payment), "Partial" (partially deducted)
     */
    @Column(nullable = false)
    private String status = "Pending";

    // Optional: Link to the payment where it was deducted (for auditing)
    private Long deductedInPaymentId;

    private Boolean isDelete = false;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}