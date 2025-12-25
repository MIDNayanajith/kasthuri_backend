package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.AttendanceDTO;
import com.enterprise.bms.enterprise_bms.entity.AttendanceEntity;
import com.enterprise.bms.enterprise_bms.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceDTO saveAttendance(AttendanceDTO dto) {
        validateAttendanceDTO(dto);
        if (attendanceRepository.existsByRecipientTypeAndRecipientIdAndAttendanceDate(
                dto.getRecipientType(), dto.getRecipientId(), dto.getAttendanceDate())) {
            throw new RuntimeException("Attendance record already exists for this recipient on this date!");
        }
        AttendanceEntity entity = toEntity(dto);
        calculateTotalHours(entity);
        entity = attendanceRepository.save(entity);
        return toDTO(entity);
    }

    public AttendanceDTO updateAttendance(Long id, AttendanceDTO dto) {
        AttendanceEntity existing = attendanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found with ID: " + id));
        updateFields(existing, dto);
        calculateTotalHours(existing);
        existing = attendanceRepository.save(existing);
        return toDTO(existing);
    }

    public void deleteAttendance(Long id) {
        AttendanceEntity entity = attendanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found with ID: " + id));
        entity.setIsDelete(true);
        attendanceRepository.save(entity);
    }

    public AttendanceDTO getAttendanceById(Long id) {
        AttendanceEntity entity = attendanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found with ID: " + id));
        return toDTO(entity);
    }

    public List<AttendanceDTO> getFilteredAttendances(String recipientType, Long recipientId, String month) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (month != null && !month.isEmpty()) {
            try {
                LocalDate monthStart = LocalDate.parse(month + "-01");
                startDate = monthStart;
                endDate = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            } catch (Exception e) {
                throw new RuntimeException("Invalid month format. Use YYYY-MM");
            }
        }
        List<AttendanceEntity> entities = attendanceRepository.findFiltered(recipientType, recipientId, startDate, endDate);
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private void validateAttendanceDTO(AttendanceDTO dto) {
        if (dto.getRecipientType() == null || (!dto.getRecipientType().equals("Driver") && !dto.getRecipientType().equals("User"))) {
            throw new RuntimeException("Invalid recipient type! Must be 'Driver' or 'User'");
        }
        if (dto.getRecipientId() == null) {
            throw new RuntimeException("Recipient ID is required!");
        }
        if (dto.getAttendanceDate() == null) {
            throw new RuntimeException("Attendance date is required!");
        }
        if (dto.getStatus() == null) {
            throw new RuntimeException("Status is required!");
        }
    }

    private void updateFields(AttendanceEntity existing, AttendanceDTO dto) {
        if (dto.getRecipientType() != null) existing.setRecipientType(dto.getRecipientType());
        if (dto.getRecipientId() != null) existing.setRecipientId(dto.getRecipientId());
        if (dto.getAttendanceDate() != null) existing.setAttendanceDate(dto.getAttendanceDate());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (dto.getCheckInTime() != null) existing.setCheckInTime(dto.getCheckInTime());
        if (dto.getCheckOutTime() != null) existing.setCheckOutTime(dto.getCheckOutTime());
        if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
        if (dto.getCreatedBy() != null) existing.setCreatedBy(dto.getCreatedBy());
    }

    private void calculateTotalHours(AttendanceEntity entity) {
        if (entity.getCheckInTime() != null && entity.getCheckOutTime() != null) {
            Duration duration = Duration.between(entity.getCheckInTime(), entity.getCheckOutTime());
            if (duration.isNegative()) {
                throw new RuntimeException("Check-out time must be after check-in time!");
            }
            BigDecimal hours = BigDecimal.valueOf(duration.toMinutes() / 60.0).setScale(2, RoundingMode.HALF_UP);
            entity.setTotalHours(hours);
        } else {
            entity.setTotalHours(null);
        }
    }

    private AttendanceEntity toEntity(AttendanceDTO dto) {
        return AttendanceEntity.builder()
                .recipientType(dto.getRecipientType())
                .recipientId(dto.getRecipientId())
                .attendanceDate(dto.getAttendanceDate())
                .status(dto.getStatus())
                .checkInTime(dto.getCheckInTime())
                .checkOutTime(dto.getCheckOutTime())
                .notes(dto.getNotes())
                .createdBy(dto.getCreatedBy())
                .isDelete(false)
                .build();
    }

    private AttendanceDTO toDTO(AttendanceEntity entity) {
        return AttendanceDTO.builder()
                .id(entity.getId())
                .recipientType(entity.getRecipientType())
                .recipientId(entity.getRecipientId())
                .attendanceDate(entity.getAttendanceDate())
                .status(entity.getStatus())
                .checkInTime(entity.getCheckInTime())
                .checkOutTime(entity.getCheckOutTime())
                .totalHours(entity.getTotalHours())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .isDelete(entity.getIsDelete())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ByteArrayInputStream generateAttendanceExcelReport(String recipientType, Long recipientId, String month) {
        List<AttendanceDTO> records = getFilteredAttendances(recipientType, recipientId, month);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance Records");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Recipient Type", "Recipient ID", "Attendance Date", "Status", "Check In Time", "Check Out Time",
                    "Total Hours", "Notes", "Created By", "Created At", "Updated At"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            for (AttendanceDTO dto : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getId() != null ? dto.getId() : 0);
                row.createCell(1).setCellValue(dto.getRecipientType() != null ? dto.getRecipientType() : "");
                row.createCell(2).setCellValue(dto.getRecipientId() != null ? dto.getRecipientId() : 0);
                row.createCell(3).setCellValue(dto.getAttendanceDate() != null ? dto.getAttendanceDate().format(dateFormatter) : "");
                row.createCell(4).setCellValue(dto.getStatus() != null ? dto.getStatus() : "");
                row.createCell(5).setCellValue(dto.getCheckInTime() != null ? dto.getCheckInTime().format(timeFormatter) : "");
                row.createCell(6).setCellValue(dto.getCheckOutTime() != null ? dto.getCheckOutTime().format(timeFormatter) : "");
                row.createCell(7).setCellValue(dto.getTotalHours() != null ? dto.getTotalHours().doubleValue() : 0.0);
                row.createCell(8).setCellValue(dto.getNotes() != null ? dto.getNotes() : "");
                row.createCell(9).setCellValue(dto.getCreatedBy() != null ? dto.getCreatedBy() : 0);
                row.createCell(10).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : "");
                row.createCell(11).setCellValue(dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : "");
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
}