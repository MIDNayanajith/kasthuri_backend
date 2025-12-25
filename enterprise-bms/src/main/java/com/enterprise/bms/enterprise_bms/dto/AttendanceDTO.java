package com.enterprise.bms.enterprise_bms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private Long id;
    private String recipientType; // "Driver" or "User"
    private Long recipientId;
    private LocalDate attendanceDate;
    private String status; // "Present", "Absent", "Leave", "Holiday"
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private BigDecimal totalHours;
    private String notes;
    private Long createdBy;
    private Boolean isDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}