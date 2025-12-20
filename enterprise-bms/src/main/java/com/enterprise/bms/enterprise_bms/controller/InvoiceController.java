// New InvoiceController.java
package com.enterprise.bms.enterprise_bms.controller;

import com.enterprise.bms.enterprise_bms.dto.InvoiceDTO;
import com.enterprise.bms.enterprise_bms.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    // Create invoice (body: { "transportIds": [1,2], "createdByUserId": 1 })
    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(@RequestBody CreateInvoiceRequest request) {
        InvoiceDTO dto = invoiceService.createInvoice(request.getTransportIds(), request.getCreatedByUserId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> downloadInvoicePdf(@PathVariable Long id) {
        ByteArrayInputStream inputStream = invoiceService.generateInvoicePdf(id);
        InputStreamResource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}

// Helper request class
class CreateInvoiceRequest {
    private List<Long> transportIds;
    private Long createdByUserId;

    // Getters and setters
    public List<Long> getTransportIds() {
        return transportIds;
    }

    public void setTransportIds(List<Long> transportIds) {
        this.transportIds = transportIds;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}