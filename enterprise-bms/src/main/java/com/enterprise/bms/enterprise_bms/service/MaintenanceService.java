// Updated MaintenanceService.java
package com.enterprise.bms.enterprise_bms.service;
import com.enterprise.bms.enterprise_bms.dto.MaintenanceDTO;
import com.enterprise.bms.enterprise_bms.entity.MaintenanceEntity;
import com.enterprise.bms.enterprise_bms.entity.OwnVehiclesEntity;
import com.enterprise.bms.enterprise_bms.repository.MaintenanceRepository;
import com.enterprise.bms.enterprise_bms.repository.OwnVehiclesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private final MaintenanceRepository maintenanceRepository;
    private final OwnVehiclesRepository ownVehiclesRepository;
    // Filtered records
    public List<MaintenanceDTO> getFilteredMaintenanceRecords(Long vehicleId, String month) {
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
        List<MaintenanceEntity> entities = maintenanceRepository.findFiltered(vehicleId, startDate, endDate);
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }
    // GET ALL (Now uses filtered with nulls)
    public List<MaintenanceDTO> getAllMaintenanceRecords() {
        return getFilteredMaintenanceRecords(null, null);
    }
    // GET BY VEHICLE ID (Now uses filtered)
    public List<MaintenanceDTO> getMaintenanceByVehicleId(Long vehicleId) {
        return getFilteredMaintenanceRecords(vehicleId, null);
    }
    // CREATE
    @Transactional
    public MaintenanceDTO createMaintenance(MaintenanceDTO dto) {
        OwnVehiclesEntity vehicle = validateVehicle(dto.getVehicleId());
        MaintenanceEntity entity = new MaintenanceEntity();
        entity.setOwnVehicle(vehicle);
        entity.setDate(dto.getDate());
        entity.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        entity.setMileage(dto.getMileage());
        entity.setQuantity(dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1);
        entity.setUnitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO);
        calculateTotalPrice(entity);
        entity = maintenanceRepository.save(entity);
        return toDTO(entity);
    }
    // GET BY ID
    public MaintenanceDTO getMaintenanceById(Long id) {
        MaintenanceEntity entity = maintenanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found or deleted"));
        return toDTO(entity);
    }
    // UPDATE
    @Transactional
    public MaintenanceDTO updateMaintenance(Long id, MaintenanceDTO dto) {
        MaintenanceEntity existing = maintenanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found"));
        // ONLY validate and change vehicle if vehicleId is actually provided in DTO
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
        // Rest of the updates (safe - only if not null)
        if (dto.getDate() != null) existing.setDate(dto.getDate());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription().trim());
        if (dto.getMileage() != null) existing.setMileage(dto.getMileage());
        if (dto.getQuantity() != null && dto.getQuantity() > 0) existing.setQuantity(dto.getQuantity());
        if (dto.getUnitPrice() != null) existing.setUnitPrice(dto.getUnitPrice());
        calculateTotalPrice(existing);
        existing = maintenanceRepository.save(existing);
        return toDTO(existing);
    }
    // DELETE (Soft)
    @Transactional
    public void deleteMaintenance(Long id) {
        MaintenanceEntity entity = maintenanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Record not found or already deleted"));
        entity.setIsDelete(true);
        maintenanceRepository.save(entity);
    }
    // Generate Excel
    public ByteArrayInputStream generateMaintenanceExcelReport(Long vehicleId, String month) {
        List<MaintenanceDTO> records = getFilteredMaintenanceRecords(vehicleId, month);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Maintenance Records");
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Vehicle Reg Number", "Vehicle ID", "Date", "Description", "Mileage", "Quantity", "Unit Price", "Total Price", "Created At", "Updated At"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            // Populate data rows
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (MaintenanceDTO dto : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getId() != null ? dto.getId().toString() : "");
                row.createCell(1).setCellValue(dto.getVehicleRegNumber() != null ? dto.getVehicleRegNumber() : "");
                row.createCell(2).setCellValue(dto.getVehicleId() != null ? dto.getVehicleId().toString() : "");
                row.createCell(3).setCellValue(dto.getDate() != null ? dto.getDate().format(formatter) : "");
                row.createCell(4).setCellValue(dto.getDescription() != null ? dto.getDescription() : "");
                row.createCell(5).setCellValue(dto.getMileage() != null ? dto.getMileage().doubleValue() : 0.0);
                row.createCell(6).setCellValue(dto.getQuantity() != null ? dto.getQuantity() : 0);
                row.createCell(7).setCellValue(dto.getUnitPrice() != null ? dto.getUnitPrice().doubleValue() : 0.0);
                row.createCell(8).setCellValue(dto.getTotalPrice() != null ? dto.getTotalPrice().doubleValue() : 0.0);
                row.createCell(9).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : "");
                row.createCell(10).setCellValue(dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : "");
            }
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // Write to ByteArrayOutputStream
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
            throw new RuntimeException("Cannot add/update maintenance for deleted vehicle");
        }
        return vehicle;
    }
    private void calculateTotalPrice(MaintenanceEntity entity) {
        int qty = entity.getQuantity() != null ? entity.getQuantity() : 1;
        BigDecimal price = entity.getUnitPrice() != null ? entity.getUnitPrice() : BigDecimal.ZERO;
        entity.setTotalPrice(price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP));
    }
    private MaintenanceDTO toDTO(MaintenanceEntity e) {
        return MaintenanceDTO.builder()
                .id(e.getId())
                .vehicleId(e.getOwnVehicle().getId())
                .vehicleRegNumber(e.getOwnVehicle().getRegNumber())
                .date(e.getDate())
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
}