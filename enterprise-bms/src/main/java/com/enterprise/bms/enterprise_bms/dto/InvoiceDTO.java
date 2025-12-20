// New InvoiceDTO.java
package com.enterprise.bms.enterprise_bms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceDTO {
    private Long id;
    private String invoiceNo;
    private LocalDate generationDate;
    private String clientName;
    private BigDecimal subtotal;
    private BigDecimal totalAdvance;
    private BigDecimal totalHeldUp;
    private BigDecimal totalBalance;
    private BigDecimal totalAmount;
    private String status;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InvoiceItemDTO> items;
}