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
import java.time.LocalTime;

@Entity
@Table(name = "tbl_attendance")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientType; // "Driver" or "User"

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private String status = "Absent"; // "Present", "Absent", "Leave", "Holiday"

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    @Column(precision = 4, scale = 2)
    private BigDecimal totalHours;

    private String notes;

    private Long createdBy;

    private Boolean isDelete = false;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}