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
    private BigDecimal vehicleUsage;

    // Initial advance payment
    @Column(name = "advance_paid", precision = 12, scale = 2)
    private BigDecimal advancePaid = BigDecimal.ZERO;

    // Total amount paid so far (cumulative)
    @Column(name = "total_paid", precision = 12, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    // Remaining balance to pay
    @Column(precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Payment Status:
     * 1 = Pending (no payment)
     * 2 = Advance Paid (partial payment)
     * 3 = Fully Paid (full payment received)
     */
    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus = 1;

    @Column(name = "vehicle_date", nullable = false)
    private LocalDate date;

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
        // Initialize totalPaid with advancePaid
        if (advancePaid != null) {
            totalPaid = advancePaid;
            balance = hireRate.subtract(advancePaid);
        } else {
            balance = hireRate;
        }
        updatePaymentStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updatePaymentStatus();
    }

    private void updatePaymentStatus() {
        if (totalPaid == null) {
            paymentStatus = 1; // Pending
            balance = hireRate;
        } else if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            paymentStatus = 1; // Pending
            balance = hireRate;
        } else if (totalPaid.compareTo(hireRate) >= 0) {
            paymentStatus = 3; // Fully Paid
            balance = BigDecimal.ZERO;
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = 2; // Advance/Partial Paid
            balance = hireRate.subtract(totalPaid);
        }
    }

    // Helper method to add a payment
    public void addPayment(BigDecimal amount) {
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }
        totalPaid = totalPaid.add(amount);
        updatePaymentStatus();
    }
}