package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.FuelDTO;
import com.enterprise.bms.enterprise_bms.entity.FuelEntity;
import com.enterprise.bms.enterprise_bms.entity.OwnVehiclesEntity;
import com.enterprise.bms.enterprise_bms.entity.TransportEntity;
import com.enterprise.bms.enterprise_bms.repository.FuelRepository;
import com.enterprise.bms.enterprise_bms.repository.OwnVehiclesRepository;
import com.enterprise.bms.enterprise_bms.repository.TransportRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuelService {

    private final FuelRepository fuelRepository;
    private final OwnVehiclesRepository ownVehiclesRepository;
    private final TransportRepository transportRepository;

    // CREATE - Save new fuel record
    public FuelDTO saveFuel(FuelDTO dto) {
        validateFuelDTO(dto);

        FuelEntity entity = toEntity(dto);
        entity = fuelRepository.save(entity);
        return toDTO(entity);
    }

    // READ - Get filtered fuel records
    public List<FuelDTO> getFilteredFuels(Long vehicleId, String month) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (month != null && !month.isEmpty()) {
            try {
                YearMonth yearMonth = YearMonth.parse(month);
                startDate = yearMonth.atDay(1);
                endDate = yearMonth.atEndOfMonth();
            } catch (Exception e) {
                throw new RuntimeException("Invalid month format. Use YYYY-MM");
            }
        }

        List<FuelEntity> entities = fuelRepository.findFiltered(vehicleId, startDate, endDate);
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // READ - Get fuel by registration number and month
    public List<FuelDTO> getFuelsByRegNumberAndMonth(String regNumber, String month) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (month != null && !month.isEmpty()) {
            try {
                YearMonth yearMonth = YearMonth.parse(month);
                startDate = yearMonth.atDay(1);
                endDate = yearMonth.atEndOfMonth();
            } catch (Exception e) {
                throw new RuntimeException("Invalid month format. Use YYYY-MM");
            }
        }

        List<FuelEntity> entities = fuelRepository.findByRegNumberAndDateRange(regNumber, startDate, endDate);
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // READ - Get fuel by ID
    public FuelDTO getFuelById(Long id) {
        FuelEntity entity = fuelRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Fuel record not found with ID: " + id));
        return toDTO(entity);
    }

    // UPDATE - Update fuel record
    public FuelDTO updateFuel(Long fuelId, FuelDTO dto) {
        FuelEntity existing = fuelRepository.findByIdAndIsDeleteFalse(fuelId)
                .orElseThrow(() -> new RuntimeException("Fuel record not found with ID: " + fuelId));

        // Update fields if provided
        if (dto.getFuelDate() != null) {
            existing.setFuelDate(dto.getFuelDate());
        }
        if (dto.getOdometerReading() != null) {
            existing.setOdometerReading(dto.getOdometerReading());
        }
        if (dto.getFuelQuantity() != null) {
            existing.setFuelQuantity(dto.getFuelQuantity());
        }
        if (dto.getTotalCost() != null) {
            existing.setTotalCost(dto.getTotalCost());
        }
        if (dto.getNotes() != null) {
            existing.setNotes(dto.getNotes());
        }

        // Update relationships if provided
        if (dto.getVehicleId() != null) {
            OwnVehiclesEntity vehicle = ownVehiclesRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + dto.getVehicleId()));
            existing.setVehicle(vehicle);
        }

        if (dto.getTripId() != null) {
            TransportEntity transport = transportRepository.findById(dto.getTripId())
                    .orElseThrow(() -> new RuntimeException("Transport not found with ID: " + dto.getTripId()));
            existing.setTransport(transport);
        } else if (dto.getTripId() == null && existing.getTransport() != null) {
            // Explicitly unset transport if null is provided
            existing.setTransport(null);
        }

        existing = fuelRepository.save(existing);
        return toDTO(existing);
    }

    // DELETE - Soft delete
    public void deleteFuel(Long fuelId) {
        FuelEntity fuel = fuelRepository.findByIdAndIsDeleteFalse(fuelId)
                .orElseThrow(() -> new RuntimeException("Fuel record not found with ID: " + fuelId));

        fuel.setIsDelete(true);
        fuelRepository.save(fuel);
    }

    // Get total fuel cost for a vehicle in a specific month
    public BigDecimal getTotalFuelCostForVehicle(Long vehicleId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        BigDecimal total = fuelRepository.getTotalFuelCostForVehicleInPeriod(vehicleId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Generate Excel report
    public ByteArrayInputStream generateFuelExcelReport(String regNumber, String month) {
        List<FuelDTO> records;

        if (regNumber != null && !regNumber.isEmpty()) {
            records = getFuelsByRegNumberAndMonth(regNumber, month);
        } else {
            records = getFilteredFuels(null, month);
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fuel Records");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "ID", "Date", "Vehicle Reg No", "Client/Trip", "Odometer Reading",
                    "Fuel Quantity (L)", "Total Cost (LKR)", "Notes", "Created At"
            };

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
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (FuelDTO dto : records) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(dto.getId() != null ? dto.getId().toString() : "");
                row.createCell(1).setCellValue(dto.getFuelDate() != null ? dto.getFuelDate().format(dateFormatter) : "");
                row.createCell(2).setCellValue(dto.getVehicleRegNumber() != null ? dto.getVehicleRegNumber() : "");
                row.createCell(3).setCellValue(dto.getClientName() != null ? dto.getClientName() : (dto.getTripDescription() != null ? dto.getTripDescription() : ""));
                row.createCell(4).setCellValue(dto.getOdometerReading() != null ? dto.getOdometerReading().doubleValue() : 0.0);
                row.createCell(5).setCellValue(dto.getFuelQuantity() != null ? dto.getFuelQuantity().doubleValue() : 0.0);
                row.createCell(6).setCellValue(dto.getTotalCost() != null ? dto.getTotalCost().doubleValue() : 0.0);
                row.createCell(7).setCellValue(dto.getNotes() != null ? dto.getNotes() : "");
                row.createCell(8).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().format(dateTimeFormatter) : "");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary row if there are records
            if (!records.isEmpty()) {
                int lastRow = sheet.getLastRowNum();
                Row summaryRow = sheet.createRow(lastRow + 2);

                Cell labelCell = summaryRow.createCell(5);
                labelCell.setCellValue("Total Fuel Cost:");
                labelCell.setCellStyle(headerStyle);

                Cell totalCell = summaryRow.createCell(6);
                BigDecimal totalCost = records.stream()
                        .map(dto -> dto.getTotalCost() != null ? dto.getTotalCost() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalCell.setCellValue(totalCost.doubleValue());

                CellStyle totalStyle = workbook.createCellStyle();
                Font totalFont = workbook.createFont();
                totalFont.setBold(true);
                totalStyle.setFont(totalFont);
                totalCell.setCellStyle(totalStyle);
            }

            // Write to ByteArrayOutputStream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Error generating fuel Excel report", e);
        }
    }

    // Validation
    private void validateFuelDTO(FuelDTO dto) {
        if (dto.getFuelDate() == null) {
            throw new RuntimeException("Fuel date is required!");
        }
        if (dto.getVehicleId() == null) {
            throw new RuntimeException("Vehicle is required!");
        }
        if (dto.getFuelQuantity() == null || dto.getFuelQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valid fuel quantity is required!");
        }
        if (dto.getTotalCost() == null || dto.getTotalCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valid total cost is required!");
        }
    }

    // Helper: DTO → Entity
    private FuelEntity toEntity(FuelDTO dto) {
        FuelEntity.FuelEntityBuilder builder = FuelEntity.builder()
                .fuelDate(dto.getFuelDate())
                .odometerReading(dto.getOdometerReading())
                .fuelQuantity(dto.getFuelQuantity())
                .totalCost(dto.getTotalCost())
                .notes(dto.getNotes())
                .isDelete(false);

        // Set vehicle
        OwnVehiclesEntity vehicle = ownVehiclesRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + dto.getVehicleId()));
        builder.vehicle(vehicle);

        // Set transport if provided
        if (dto.getTripId() != null) {
            TransportEntity transport = transportRepository.findById(dto.getTripId())
                    .orElseThrow(() -> new RuntimeException("Transport not found with ID: " + dto.getTripId()));
            builder.transport(transport);
        }

        return builder.build();
    }

    // Helper: Entity → DTO
    private FuelDTO toDTO(FuelEntity entity) {
        return FuelDTO.builder()
                .id(entity.getId())
                .fuelDate(entity.getFuelDate())
                .vehicleId(entity.getVehicle() != null ? entity.getVehicle().getId() : null)
                .tripId(entity.getTransport() != null ? entity.getTransport().getId() : null)
                .odometerReading(entity.getOdometerReading())
                .fuelQuantity(entity.getFuelQuantity())
                .totalCost(entity.getTotalCost())
                .notes(entity.getNotes())
                .isDelete(entity.getIsDelete())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .vehicleRegNumber(entity.getVehicle() != null ? entity.getVehicle().getRegNumber() : null)
                .clientName(entity.getTransport() != null ? entity.getTransport().getClientName() : null)
                .tripDescription(entity.getTransport() != null ? entity.getTransport().getDescription() : null)
                .build();
    }

    //for dashboard
    public BigDecimal getTotalFuelCostForPeriod(LocalDate start, LocalDate end) {
        return fuelRepository.getTotalFuelCostForPeriod(start, end);
    }
}