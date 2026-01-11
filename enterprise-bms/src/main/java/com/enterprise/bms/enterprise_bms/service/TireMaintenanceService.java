package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.TireMaintenanceDTO;
import com.enterprise.bms.enterprise_bms.entity.TireMaintenanceEntity;
import com.enterprise.bms.enterprise_bms.entity.OwnVehiclesEntity;
import com.enterprise.bms.enterprise_bms.repository.OwnVehiclesRepository;
import com.enterprise.bms.enterprise_bms.repository.TireMaintenanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TireMaintenanceService {

    private final TireMaintenanceRepository tireMaintenanceRepository;
    private final OwnVehiclesRepository ownVehiclesRepository;

    // Filtered records
    public List<TireMaintenanceDTO> getFilteredTireMaintenanceRecords(Long vehicleId, String month) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (month != null && !month.isEmpty()) {
            try {
                startDate = LocalDate.parse(month + "-01");
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            } catch (Exception e) {
                throw new RuntimeException("Invalid month format. Use YYYY-MM");
            }
        }
        List<TireMaintenanceEntity> entities = tireMaintenanceRepository.findFiltered(vehicleId, startDate, endDate);
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // GET ALL (Uses filtered with nulls)
    public List<TireMaintenanceDTO> getAllTireMaintenanceRecords() {
        return getFilteredTireMaintenanceRecords(null, null);
    }

    // GET BY VEHICLE ID (Uses filtered)
    public List<TireMaintenanceDTO> getTireMaintenanceByVehicleId(Long vehicleId) {
        return getFilteredTireMaintenanceRecords(vehicleId, null);
    }

    // CREATE - Manual totalPrice entry
    @Transactional
    public TireMaintenanceDTO createTireMaintenance(TireMaintenanceDTO dto) {
        if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
            throw new RuntimeException("Position is required for tire maintenance");
        }
        if (dto.getTotalPrice() == null || dto.getTotalPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Total price is required and must be >= 0");
        }

        OwnVehiclesEntity vehicle = validateVehicle(dto.getVehicleId());

        TireMaintenanceEntity entity = new TireMaintenanceEntity();
        entity.setOwnVehicle(vehicle);
        entity.setPosition(dto.getPosition().trim().toUpperCase());  // Normalize position
        entity.setDate(dto.getDate());
        entity.setTireBrand(dto.getTireBrand());
        entity.setTireSize(dto.getTireSize());
        entity.setSerialNumber(dto.getSerialNumber());
        entity.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        entity.setMileage(dto.getMileage());
        entity.setQuantity(dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1);
        entity.setUnitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO);

        // Set total price manually from DTO (no auto-calculation)
        entity.setTotalPrice(dto.getTotalPrice());

        entity = tireMaintenanceRepository.save(entity);
        return toDTO(entity);
    }

    // GET BY ID
    public TireMaintenanceDTO getTireMaintenanceById(Long id) {
        TireMaintenanceEntity entity = tireMaintenanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Tire maintenance record not found or deleted"));
        return toDTO(entity);
    }

    // UPDATE - Manual totalPrice entry
    @Transactional
    public TireMaintenanceDTO updateTireMaintenance(Long id, TireMaintenanceDTO dto) {
        TireMaintenanceEntity existing = tireMaintenanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Tire maintenance record not found"));

        // Update vehicle if provided
        if (dto.getVehicleId() != null) {
            if (!existing.getOwnVehicle().getId().equals(dto.getVehicleId())) {
                OwnVehiclesEntity newVehicle = ownVehiclesRepository.findById(dto.getVehicleId())
                        .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + dto.getVehicleId()));
                if (Boolean.TRUE.equals(newVehicle.getIsDelete())) {
                    throw new RuntimeException("Cannot assign deleted vehicle");
                }
                existing.setOwnVehicle(newVehicle);
            }
        }

        // Update other fields if provided
        if (dto.getPosition() != null && !dto.getPosition().trim().isEmpty()) {
            existing.setPosition(dto.getPosition().trim().toUpperCase());
        }
        if (dto.getDate() != null) existing.setDate(dto.getDate());
        if (dto.getTireBrand() != null) existing.setTireBrand(dto.getTireBrand());
        if (dto.getTireSize() != null) existing.setTireSize(dto.getTireSize());
        if (dto.getSerialNumber() != null) existing.setSerialNumber(dto.getSerialNumber());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription().trim());
        if (dto.getMileage() != null) existing.setMileage(dto.getMileage());
        if (dto.getQuantity() != null && dto.getQuantity() > 0) existing.setQuantity(dto.getQuantity());
        if (dto.getUnitPrice() != null) existing.setUnitPrice(dto.getUnitPrice());

        // Allow manual override of total price (only if provided)
        if (dto.getTotalPrice() != null) {
            if (dto.getTotalPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Total price cannot be negative");
            }
            existing.setTotalPrice(dto.getTotalPrice());
        }

        existing = tireMaintenanceRepository.save(existing);
        return toDTO(existing);
    }

    // DELETE (Soft)
    @Transactional
    public void deleteTireMaintenance(Long id) {
        TireMaintenanceEntity entity = tireMaintenanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Record not found or already deleted"));
        entity.setIsDelete(true);
        tireMaintenanceRepository.save(entity);
    }

    // Generate Excel Report
    public ByteArrayInputStream generateTireMaintenanceExcelReport(Long vehicleId, String month) {
        List<TireMaintenanceDTO> records = getFilteredTireMaintenanceRecords(vehicleId, month);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tire Maintenance Records");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Vehicle Reg Number", "Vehicle ID", "Position", "Date", "Tire Brand", "Tire Size", "Serial Number", "Description", "Mileage", "Quantity", "Unit Price", "Total Price", "Created At", "Updated At"};
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (TireMaintenanceDTO dto : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getId() != null ? dto.getId().toString() : "");
                row.createCell(1).setCellValue(dto.getVehicleRegNumber() != null ? dto.getVehicleRegNumber() : "");
                row.createCell(2).setCellValue(dto.getVehicleId() != null ? dto.getVehicleId().toString() : "");
                row.createCell(3).setCellValue(dto.getPosition() != null ? dto.getPosition() : "");
                row.createCell(4).setCellValue(dto.getDate() != null ? dto.getDate().format(formatter) : "");
                row.createCell(5).setCellValue(dto.getTireBrand() != null ? dto.getTireBrand() : "");
                row.createCell(6).setCellValue(dto.getTireSize() != null ? dto.getTireSize() : "");
                row.createCell(7).setCellValue(dto.getSerialNumber() != null ? dto.getSerialNumber() : "");
                row.createCell(8).setCellValue(dto.getDescription() != null ? dto.getDescription() : "");
                row.createCell(9).setCellValue(dto.getMileage() != null ? dto.getMileage().doubleValue() : 0.0);
                row.createCell(10).setCellValue(dto.getQuantity() != null ? dto.getQuantity() : 0);
                row.createCell(11).setCellValue(dto.getUnitPrice() != null ? dto.getUnitPrice().doubleValue() : 0.0);
                row.createCell(12).setCellValue(dto.getTotalPrice() != null ? dto.getTotalPrice().doubleValue() : 0.0);
                row.createCell(13).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : "");
                row.createCell(14).setCellValue(dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : "");
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

    // Helper Methods
    private OwnVehiclesEntity validateVehicle(Long vehicleId) {
        OwnVehiclesEntity vehicle = ownVehiclesRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + vehicleId));
        if (Boolean.TRUE.equals(vehicle.getIsDelete())) {
            throw new RuntimeException("Cannot add/update tire maintenance for deleted vehicle");
        }
        return vehicle;
    }

    private TireMaintenanceDTO toDTO(TireMaintenanceEntity e) {
        return TireMaintenanceDTO.builder()
                .id(e.getId())
                .vehicleId(e.getOwnVehicle().getId())
                .vehicleRegNumber(e.getOwnVehicle().getRegNumber())
                .position(e.getPosition())
                .date(e.getDate())
                .tireBrand(e.getTireBrand())
                .tireSize(e.getTireSize())
                .serialNumber(e.getSerialNumber())
                .description(e.getDescription())
                .mileage(e.getMileage())
                .quantity(e.getQuantity())
                .unitPrice(e.getUnitPrice())
                .totalPrice(e.getTotalPrice())
                .isDelete(e.getIsDelete())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    //for dashboard
    public BigDecimal getTotalTireMaintenanceCostForPeriod(LocalDate start, LocalDate end) {
        return tireMaintenanceRepository.getTotalTireMaintenanceCostForPeriod(start, end);
    }
}