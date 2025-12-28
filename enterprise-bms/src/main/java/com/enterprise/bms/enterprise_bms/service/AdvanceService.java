package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.AdvanceDTO;
import com.enterprise.bms.enterprise_bms.entity.AdvanceEntity;
import com.enterprise.bms.enterprise_bms.repository.AdvanceRepository;
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
public class AdvanceService {
    private final AdvanceRepository advanceRepository;

    public AdvanceDTO saveAdvance(AdvanceDTO dto) {
        validateAdvanceDTO(dto);
        AdvanceEntity entity = toEntity(dto);
        entity = advanceRepository.save(entity);
        return toDTO(entity);
    }

    public AdvanceDTO updateAdvance(Long id, AdvanceDTO dto) {
        AdvanceEntity existing = advanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Advance not found with ID: " + id));
        updateFields(existing, dto);
        existing = advanceRepository.save(existing);
        return toDTO(existing);
    }

    public void deleteAdvance(Long id) {
        AdvanceEntity entity = advanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Advance not found with ID: " + id));
        entity.setIsDelete(true);
        advanceRepository.save(entity);
    }

    public AdvanceDTO getAdvanceById(Long id) {
        AdvanceEntity entity = advanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Advance not found with ID: " + id));
        return toDTO(entity);
    }

    public List<AdvanceDTO> getFilteredAdvances(String recipientType, Long recipientId, String month) {
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
        List<AdvanceEntity> entities = advanceRepository.findFiltered(recipientType, recipientId, startDate, endDate);
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Helper: Get total pending advances for a period
    public BigDecimal getTotalPendingAdvances(String recipientType, Long recipientId, Integer periodMonth, Integer periodYear) {
        LocalDate startDate = LocalDate.of(periodYear, periodMonth, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<AdvanceEntity> pendings = advanceRepository.findPendingByRecipientAndPeriod(recipientType, recipientId, startDate, endDate);
        return pendings.stream()
                .map(AdvanceEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markAdvancesAsDeducted(Long paymentId, String recipientType, Long recipientId, Integer periodMonth, Integer periodYear) {
        LocalDate startDate = LocalDate.of(periodYear, periodMonth, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<AdvanceEntity> pendings = advanceRepository.findPendingByRecipientAndPeriod(recipientType, recipientId, startDate, endDate);
        for (AdvanceEntity adv : pendings) {
            adv.setStatus("Deducted");
            adv.setDeductedInPaymentId(paymentId);
            advanceRepository.save(adv);
        }
    }

    public void unmarkAdvancesForPayment(Long paymentId) {
        List<AdvanceEntity> deducted = advanceRepository.findByDeductedInPaymentId(paymentId);
        for (AdvanceEntity adv : deducted) {
            adv.setStatus("Pending");
            adv.setDeductedInPaymentId(null);
            advanceRepository.save(adv);
        }
    }

    private void validateAdvanceDTO(AdvanceDTO dto) {
        if (dto.getRecipientType() == null || (!dto.getRecipientType().equals("Driver") && !dto.getRecipientType().equals("User"))) {
            throw new RuntimeException("Invalid recipient type! Must be 'Driver' or 'User'");
        }
        if (dto.getRecipientId() == null) {
            throw new RuntimeException("Recipient ID is required!");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount is required and must be positive!");
        }
        if (dto.getAdvanceDate() == null) {
            throw new RuntimeException("Advance date is required!");
        }
        if (dto.getStatus() == null) {
            throw new RuntimeException("Status is required!");
        }
    }

    private void updateFields(AdvanceEntity existing, AdvanceDTO dto) {
        if (dto.getRecipientType() != null) existing.setRecipientType(dto.getRecipientType());
        if (dto.getRecipientId() != null) existing.setRecipientId(dto.getRecipientId());
        if (dto.getAmount() != null) existing.setAmount(dto.getAmount());
        if (dto.getAdvanceDate() != null) existing.setAdvanceDate(dto.getAdvanceDate());
        if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
        if (dto.getCreatedBy() != null) existing.setCreatedBy(dto.getCreatedBy());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (dto.getDeductedInPaymentId() != null) existing.setDeductedInPaymentId(dto.getDeductedInPaymentId());
    }

    private AdvanceEntity toEntity(AdvanceDTO dto) {
        return AdvanceEntity.builder()
                .recipientType(dto.getRecipientType())
                .recipientId(dto.getRecipientId())
                .amount(dto.getAmount())
                .advanceDate(dto.getAdvanceDate())
                .notes(dto.getNotes())
                .createdBy(dto.getCreatedBy())
                .status(dto.getStatus())
                .deductedInPaymentId(dto.getDeductedInPaymentId())
                .isDelete(false)
                .build();
    }

    private AdvanceDTO toDTO(AdvanceEntity entity) {
        return AdvanceDTO.builder()
                .id(entity.getId())
                .recipientType(entity.getRecipientType())
                .recipientId(entity.getRecipientId())
                .amount(entity.getAmount())
                .advanceDate(entity.getAdvanceDate())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .status(entity.getStatus())
                .deductedInPaymentId(entity.getDeductedInPaymentId())
                .isDelete(entity.getIsDelete())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ByteArrayInputStream generateAdvancesExcelReport(String recipientType, Long recipientId, String month) {
        List<AdvanceDTO> records = getFilteredAdvances(recipientType, recipientId, month);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Advances Records");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Recipient Type", "Recipient ID", "Amount", "Advance Date", "Notes",
                    "Created By", "Status", "Deducted In Payment ID", "Created At", "Updated At"};
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
            for (AdvanceDTO dto : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getId() != null ? dto.getId() : 0);
                row.createCell(1).setCellValue(dto.getRecipientType() != null ? dto.getRecipientType() : "");
                row.createCell(2).setCellValue(dto.getRecipientId() != null ? dto.getRecipientId() : 0);
                row.createCell(3).setCellValue(dto.getAmount() != null ? dto.getAmount().doubleValue() : 0.0);
                row.createCell(4).setCellValue(dto.getAdvanceDate() != null ? dto.getAdvanceDate().format(dateFormatter) : "");
                row.createCell(5).setCellValue(dto.getNotes() != null ? dto.getNotes() : "");
                row.createCell(6).setCellValue(dto.getCreatedBy() != null ? dto.getCreatedBy() : 0);
                row.createCell(7).setCellValue(dto.getStatus() != null ? dto.getStatus() : "");
                row.createCell(8).setCellValue(dto.getDeductedInPaymentId() != null ? dto.getDeductedInPaymentId() : 0);
                row.createCell(9).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : "");
                row.createCell(10).setCellValue(dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : "");
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