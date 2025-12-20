// New InvoiceItemEntity.java
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
@Table(name = "tbl_invoice_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false, foreignKey = @ForeignKey(name = "FK_invoice_item_invoice"))
    private InvoiceEntity invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_id", nullable = false, foreignKey = @ForeignKey(name = "FK_invoice_item_transport"))
    private TransportEntity transport;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "vehicle_reg_no", nullable = false, length = 20)
    private String vehicleRegNo;

    @Column(name = "particulars", columnDefinition = "TEXT")
    private String particulars;

    @Column(name = "rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal rate;

    @Column(name = "advance", precision = 12, scale = 2)
    private BigDecimal advance = BigDecimal.ZERO;

    @Column(name = "held_up", precision = 12, scale = 2)
    private BigDecimal heldUp = BigDecimal.ZERO;

    @Column(name = "balance", precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

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