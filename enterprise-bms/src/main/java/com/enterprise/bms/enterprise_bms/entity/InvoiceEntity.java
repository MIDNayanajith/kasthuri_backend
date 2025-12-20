// New InvoiceEntity.java
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
@Table(name = "tbl_invoices")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_no", nullable = false, unique = true, length = 50)
    private String invoiceNo;

    @Column(name = "generation_date", nullable = false)
    private LocalDate generationDate;

    @Column(name = "client_name", nullable = false, length = 150)
    private String clientName;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "total_advance", precision = 12, scale = 2)
    private BigDecimal totalAdvance = BigDecimal.ZERO;

    @Column(name = "total_held_up", precision = 12, scale = 2)
    private BigDecimal totalHeldUp = BigDecimal.ZERO;

    @Column(name = "total_balance", precision = 12, scale = 2)
    private BigDecimal totalBalance = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private String status = "Draft"; // ENUM('Draft', 'Sent', 'Paid', 'Overdue')

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "FK_invoice_user"))
    private UserEntity createdBy; // Assuming UserEntity exists; adjust if needed

    @Column(name = "is_delete")
    private Boolean isDeleted = false;

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