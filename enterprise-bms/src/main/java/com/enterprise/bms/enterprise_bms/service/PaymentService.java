package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.PaymentsDTO;
import com.enterprise.bms.enterprise_bms.entity.PaymentsEntity;
import com.enterprise.bms.enterprise_bms.repository.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentsRepository paymentsRepository;
    private final AdvanceService advanceService;

    public PaymentsDTO savePayment(PaymentsDTO dto) {
        validatePaymentsDTO(dto);
        if (paymentsRepository.existsByRecipientTypeAndRecipientIdAndPeriodMonthAndPeriodYear(
                dto.getRecipientType(), dto.getRecipientId(), dto.getPeriodMonth(), dto.getPeriodYear())) {
            throw new RuntimeException("Payment record already exists for this recipient in this period!");
        }
        PaymentsEntity entity = toEntity(dto);
        BigDecimal pendingAdvances = advanceService.getTotalPendingAdvances(
                dto.getRecipientType(), dto.getRecipientId(), dto.getPeriodMonth(), dto.getPeriodYear());
        entity.setAdvancesDeducted(pendingAdvances);
        calculateNetPay(entity);
        entity = paymentsRepository.save(entity);
        advanceService.markAdvancesAsDeducted(entity.getId(), entity.getRecipientType(), entity.getRecipientId(),
                entity.getPeriodMonth(), entity.getPeriodYear());
        return toDTO(entity);
    }

    public PaymentsDTO updatePayment(Long id, PaymentsDTO dto) {
        PaymentsEntity existing = paymentsRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
        updateFields(existing, dto);
        calculateNetPay(existing);
        existing = paymentsRepository.save(existing);
        return toDTO(existing);
    }

    public void deletePayment(Long id) {
        PaymentsEntity entity = paymentsRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
        advanceService.unmarkAdvancesForPayment(id);
        entity.setIsDelete(true);
        paymentsRepository.save(entity);
    }

    public PaymentsDTO getPaymentById(Long id) {
        PaymentsEntity entity = paymentsRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
        return toDTO(entity);
    }

    public List<PaymentsDTO> getFilteredPayments(String recipientType, Long recipientId, String month) {
        Integer periodMonth = null;
        Integer periodYear = null;
        if (month != null && !month.isEmpty()) {
            try {
                String[] parts = month.split("-");
                periodYear = Integer.parseInt(parts[0]);
                periodMonth = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                throw new RuntimeException("Invalid month format. Use YYYY-MM");
            }
        }
        List<PaymentsEntity> entities = paymentsRepository.findFiltered(recipientType, recipientId, periodMonth, periodYear);
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private void validatePaymentsDTO(PaymentsDTO dto) {
        if (dto.getRecipientType() == null || (!dto.getRecipientType().equals("Driver") && !dto.getRecipientType().equals("User"))) {
            throw new RuntimeException("Invalid recipient type! Must be 'Driver' or 'User'");
        }
        if (dto.getRecipientId() == null) {
            throw new RuntimeException("Recipient ID is required!");
        }
        if (dto.getPeriodMonth() == null || dto.getPeriodYear() == null) {
            throw new RuntimeException("Period month and year are required!");
        }
        if (dto.getBaseAmount() == null) {
            throw new RuntimeException("Base amount is required!");
        }
        if (dto.getStatus() == null) {
            throw new RuntimeException("Status is required!");
        }
    }

    private void updateFields(PaymentsEntity existing, PaymentsDTO dto) {
        if (dto.getRecipientType() != null) existing.setRecipientType(dto.getRecipientType());
        if (dto.getRecipientId() != null) existing.setRecipientId(dto.getRecipientId());
        if (dto.getPeriodMonth() != null) existing.setPeriodMonth(dto.getPeriodMonth());
        if (dto.getPeriodYear() != null) existing.setPeriodYear(dto.getPeriodYear());
        if (dto.getBaseAmount() != null) existing.setBaseAmount(dto.getBaseAmount());
        if (dto.getDeductions() != null) existing.setDeductions(dto.getDeductions());
        if (dto.getAdvancesDeducted() != null) existing.setAdvancesDeducted(dto.getAdvancesDeducted());
        if (dto.getPaymentDate() != null) existing.setPaymentDate(dto.getPaymentDate());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
        if (dto.getCreatedBy() != null) existing.setCreatedBy(dto.getCreatedBy());
    }

    private void calculateNetPay(PaymentsEntity entity) {
        BigDecimal net = entity.getBaseAmount()
                .subtract(entity.getDeductions() != null ? entity.getDeductions() : BigDecimal.ZERO)
                .subtract(entity.getAdvancesDeducted() != null ? entity.getAdvancesDeducted() : BigDecimal.ZERO);
        entity.setNetPay(net);
    }

    private PaymentsEntity toEntity(PaymentsDTO dto) {
        return PaymentsEntity.builder()
                .recipientType(dto.getRecipientType())
                .recipientId(dto.getRecipientId())
                .periodMonth(dto.getPeriodMonth())
                .periodYear(dto.getPeriodYear())
                .baseAmount(dto.getBaseAmount())
                .deductions(dto.getDeductions())
                .advancesDeducted(dto.getAdvancesDeducted())
                .paymentDate(dto.getPaymentDate())
                .status(dto.getStatus())
                .notes(dto.getNotes())
                .createdBy(dto.getCreatedBy())
                .isDelete(false)
                .build();
    }

    private PaymentsDTO toDTO(PaymentsEntity entity) {
        return PaymentsDTO.builder()
                .id(entity.getId())
                .recipientType(entity.getRecipientType())
                .recipientId(entity.getRecipientId())
                .periodMonth(entity.getPeriodMonth())
                .periodYear(entity.getPeriodYear())
                .baseAmount(entity.getBaseAmount())
                .deductions(entity.getDeductions())
                .advancesDeducted(entity.getAdvancesDeducted())
                .netPay(entity.getNetPay())
                .paymentDate(entity.getPaymentDate())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .isDelete(entity.getIsDelete())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ByteArrayInputStream generatePaymentsExcelReport(String recipientType, Long recipientId, String month) {
        List<PaymentsDTO> records = getFilteredPayments(recipientType, recipientId, month);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payments Records");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Recipient Type", "Recipient ID", "Period Month", "Period Year", "Base Amount",
                    "Deductions", "Advances Deducted", "Net Pay", "Payment Date", "Status", "Notes", "Created By",
                    "Created At", "Updated At"};
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
            for (PaymentsDTO dto : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getId() != null ? dto.getId() : 0);
                row.createCell(1).setCellValue(dto.getRecipientType() != null ? dto.getRecipientType() : "");
                row.createCell(2).setCellValue(dto.getRecipientId() != null ? dto.getRecipientId() : 0);
                row.createCell(3).setCellValue(dto.getPeriodMonth() != null ? dto.getPeriodMonth() : 0);
                row.createCell(4).setCellValue(dto.getPeriodYear() != null ? dto.getPeriodYear() : 0);
                row.createCell(5).setCellValue(dto.getBaseAmount() != null ? dto.getBaseAmount().doubleValue() : 0.0);
                row.createCell(6).setCellValue(dto.getDeductions() != null ? dto.getDeductions().doubleValue() : 0.0);
                row.createCell(7).setCellValue(dto.getAdvancesDeducted() != null ? dto.getAdvancesDeducted().doubleValue() : 0.0);
                row.createCell(8).setCellValue(dto.getNetPay() != null ? dto.getNetPay().doubleValue() : 0.0);
                row.createCell(9).setCellValue(dto.getPaymentDate() != null ? dto.getPaymentDate().format(dateFormatter) : "");
                row.createCell(10).setCellValue(dto.getStatus() != null ? dto.getStatus() : "");
                row.createCell(11).setCellValue(dto.getNotes() != null ? dto.getNotes() : "");
                row.createCell(12).setCellValue(dto.getCreatedBy() != null ? dto.getCreatedBy() : 0);
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
}