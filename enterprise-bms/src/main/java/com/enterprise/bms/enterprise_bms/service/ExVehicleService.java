package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.ExVehiclesDTO;
import com.enterprise.bms.enterprise_bms.entity.ExVehiclesEntity;
import com.enterprise.bms.enterprise_bms.repository.ExVehiclesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.YearMonth;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ExVehicleService {
    private final ExVehiclesRepository exVehiclesRepository;

    // CREATE
    public ExVehiclesDTO saveExVehicle(ExVehiclesDTO dto) {
        validateExVehicleDTO(dto);
        ExVehiclesEntity entity = toEntity(dto);

        // Set initial payments
        if (dto.getAdvancePaid() == null) {
            entity.setAdvancePaid(BigDecimal.ZERO);
            entity.setTotalPaid(BigDecimal.ZERO);
        } else {
            entity.setAdvancePaid(dto.getAdvancePaid());
            entity.setTotalPaid(dto.getAdvancePaid());
        }

        entity = exVehiclesRepository.save(entity);
        return toDTO(entity);
    }

    // READ
    public List<ExVehiclesDTO> getAllExVehicles() {
        return exVehiclesRepository.findAllActive()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExVehiclesDTO getExVehicleById(Long id) {
        ExVehiclesEntity entity = exVehiclesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("External vehicle not found"));
        if (Boolean.TRUE.equals(entity.getIsDelete())) {
            throw new RuntimeException("External vehicle is deleted");
        }
        return toDTO(entity);
    }

    // FILTERED READ
    public List<ExVehiclesDTO> getFilteredExVehicles(String regNumber, String month) {
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
        List<ExVehiclesEntity> entities = exVehiclesRepository.findFiltered(regNumber, startDate, endDate);
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // UPDATE VEHICLE DETAILS
    public ExVehiclesDTO updateExVehicle(Long id, ExVehiclesDTO dto) {
        ExVehiclesEntity entity = exVehiclesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("External vehicle not found"));

        if (Boolean.TRUE.equals(entity.getIsDelete())) {
            throw new RuntimeException("Cannot update deleted vehicle");
        }

        // Update basic details
        if (dto.getRegNumber() != null) entity.setRegNumber(dto.getRegNumber().trim());
        if (dto.getOwnerName() != null) entity.setOwnerName(dto.getOwnerName());
        if (dto.getOwnerContact() != null) entity.setOwnerContact(dto.getOwnerContact());
        if (dto.getHireRate() != null) entity.setHireRate(dto.getHireRate());
        if (dto.getVehicleUsage() != null) entity.setVehicleUsage(dto.getVehicleUsage());
        if (dto.getDate() != null) entity.setDate(dto.getDate());

        // Handle payment update if provided
        if (dto.getNewPayment() != null && dto.getNewPayment().compareTo(BigDecimal.ZERO) > 0) {
            entity.addPayment(dto.getNewPayment());
        }

        entity = exVehiclesRepository.save(entity);
        return toDTO(entity);
    }

    // MAKE PAYMENT
    public ExVehiclesDTO makePayment(Long id, BigDecimal paymentAmount) {
        ExVehiclesEntity entity = exVehiclesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("External vehicle not found"));

        if (Boolean.TRUE.equals(entity.getIsDelete())) {
            throw new RuntimeException("Cannot make payment for deleted vehicle");
        }

        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be positive");
        }

        // Check if payment exceeds balance
        BigDecimal currentBalance = entity.getBalance();
        if (paymentAmount.compareTo(currentBalance) > 0) {
            throw new RuntimeException("Payment amount exceeds balance. Balance: " + currentBalance);
        }

        // Add payment
        entity.addPayment(paymentAmount);

        entity = exVehiclesRepository.save(entity);
        return toDTO(entity);
    }

    // DELETE (Soft)
    public void deleteExVehicle(Long id) {
        ExVehiclesEntity entity = exVehiclesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("External vehicle not found"));
        entity.setIsDelete(true);
        exVehiclesRepository.save(entity);
    }

    // ------------------- Helpers -------------------
    private void validateExVehicleDTO(ExVehiclesDTO dto) {
        if (dto.getRegNumber() == null || dto.getRegNumber().isBlank()) {
            throw new RuntimeException("Registration number is required");
        }
        if (dto.getOwnerName() == null || dto.getOwnerName().isBlank()) {
            throw new RuntimeException("Owner name is required");
        }
        if (dto.getOwnerContact() == null || dto.getOwnerContact().isBlank()) {
            throw new RuntimeException("Owner contact is required");
        }
        if (dto.getHireRate() == null || dto.getHireRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valid hire rate is required");
        }
        if (dto.getDate() == null) {
            throw new RuntimeException("Date is required");
        }
    }

    private ExVehiclesEntity toEntity(ExVehiclesDTO dto) {
        return ExVehiclesEntity.builder()
                .regNumber(dto.getRegNumber().trim())
                .ownerName(dto.getOwnerName().trim())
                .ownerContact(dto.getOwnerContact().trim())
                .hireRate(dto.getHireRate())
                .vehicleUsage(dto.getVehicleUsage())
                .date(dto.getDate())
                .isDelete(false)
                .build();
    }

    private ExVehiclesDTO toDTO(ExVehiclesEntity entity) {
        return ExVehiclesDTO.builder()
                .id(entity.getId())
                .regNumber(entity.getRegNumber())
                .ownerName(entity.getOwnerName())
                .ownerContact(entity.getOwnerContact())
                .hireRate(entity.getHireRate())
                .vehicleUsage(entity.getVehicleUsage())
                .advancePaid(entity.getAdvancePaid())
                .totalPaid(entity.getTotalPaid())
                .balance(entity.getBalance())
                .paymentStatus(entity.getPaymentStatus())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ByteArrayInputStream generateExVehiclesExcelReport(String regNumber, String month) {
        List<ExVehiclesDTO> records = getFilteredExVehicles(regNumber, month);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ExVehicles Records");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Reg Number", "Owner Name", "Owner Contact", "Hire Rate", "Vehicle Usage", "Advance Paid", "Total Paid", "Balance", "Payment Status", "Date", "Created At", "Updated At"};

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

            for (ExVehiclesDTO dto : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getId() != null ? dto.getId() : 0);
                row.createCell(1).setCellValue(dto.getRegNumber() != null ? dto.getRegNumber() : "");
                row.createCell(2).setCellValue(dto.getOwnerName() != null ? dto.getOwnerName() : "");
                row.createCell(3).setCellValue(dto.getOwnerContact() != null ? dto.getOwnerContact() : "");
                row.createCell(4).setCellValue(dto.getHireRate() != null ? dto.getHireRate().doubleValue() : 0.0);
                row.createCell(5).setCellValue(dto.getVehicleUsage() != null ? dto.getVehicleUsage().doubleValue() : 0.0);
                row.createCell(6).setCellValue(dto.getAdvancePaid() != null ? dto.getAdvancePaid().doubleValue() : 0.0);
                row.createCell(7).setCellValue(dto.getTotalPaid() != null ? dto.getTotalPaid().doubleValue() : 0.0);
                row.createCell(8).setCellValue(dto.getBalance() != null ? dto.getBalance().doubleValue() : 0.0);

                String statusLabel = "";
                if (dto.getPaymentStatus() == 1) statusLabel = "Pending";
                else if (dto.getPaymentStatus() == 2) statusLabel = "Partial Paid";
                else if (dto.getPaymentStatus() == 3) statusLabel = "Fully Paid";
                row.createCell(9).setCellValue(statusLabel);

                row.createCell(10).setCellValue(dto.getDate() != null ? dto.getDate().format(dateFormatter) : "");
                row.createCell(11).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : "");
                row.createCell(12).setCellValue(dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : "");
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