// New InvoiceItemDTO.java
package com.enterprise.bms.enterprise_bms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceItemDTO {
    private Long id;
    private Long invoiceId;
    private Long transportId;
    private LocalDate date;
    private String vehicleRegNo;
    private String particulars;
    private BigDecimal rate;
    private BigDecimal advance;
    private BigDecimal heldUp;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}