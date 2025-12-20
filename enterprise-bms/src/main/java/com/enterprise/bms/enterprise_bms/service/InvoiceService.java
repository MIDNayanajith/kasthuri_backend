// Updated InvoiceService.java with proper bold font setting
package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.InvoiceDTO;
import com.enterprise.bms.enterprise_bms.dto.InvoiceItemDTO;
import com.enterprise.bms.enterprise_bms.entity.InvoiceEntity;
import com.enterprise.bms.enterprise_bms.entity.InvoiceItemEntity;
import com.enterprise.bms.enterprise_bms.entity.TransportEntity;
import com.enterprise.bms.enterprise_bms.entity.UserEntity; // Assuming exists; adjust if needed
import com.enterprise.bms.enterprise_bms.repository.InvoiceItemRepository;
import com.enterprise.bms.enterprise_bms.repository.InvoiceRepository;
import com.enterprise.bms.enterprise_bms.repository.TransportRepository;
import com.enterprise.bms.enterprise_bms.repository.UserRepository; // Assuming exists
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final TransportRepository transportRepository;
    private final UserRepository userRepository; // Assuming exists for createdBy

    // Create invoice from list of transport IDs
    public InvoiceDTO createInvoice(List<Long> transportIds, Long createdByUserId) {
        if (transportIds == null || transportIds.isEmpty()) {
            throw new RuntimeException("At least one transport ID is required");
        }
        // Fetch transports
        List<TransportEntity> transports = transportRepository.findAllById(transportIds);
        if (transports.size() != transportIds.size()) {
            throw new RuntimeException("Some transports not found");
        }
        // Validate: all not invoiced, completed, same client
        Set<String> clientNames = transports.stream().map(TransportEntity::getClientName).collect(Collectors.toSet());
        if (clientNames.size() != 1) {
            throw new RuntimeException("All transports must have the same client name");
        }
        String clientName = clientNames.iterator().next();
        for (TransportEntity t : transports) {
            if (t.getInvoiceId() != null) {
                throw new RuntimeException("Transport " + t.getId() + " is already invoiced");
            }
            if (t.getTripStatus() != 2) { // Assuming 2 = Completed
                throw new RuntimeException("Transport " + t.getId() + " is not completed");
            }
        }
        // Fetch user
        UserEntity createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Generate invoice_no
        String year = Year.now().toString();
        String maxNo = invoiceRepository.findMaxInvoiceNoForYear(year);
        int seq = (maxNo == null) ? 1 : Integer.parseInt(maxNo.split("-")[2]) + 1;
        String invoiceNo = "INV-" + year + "-" + String.format("%03d", seq);
        // Create invoice
        InvoiceEntity invoice = InvoiceEntity.builder()
                .invoiceNo(invoiceNo)
                .generationDate(LocalDate.now())
                .clientName(clientName)
                .status("Draft")
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
        invoice = invoiceRepository.save(invoice);
        // Create items and calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalAdvance = BigDecimal.ZERO;
        BigDecimal totalHeldUp = BigDecimal.ZERO;
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (TransportEntity t : transports) {
            String regNo = (t.getOwnVehicle() != null) ? t.getOwnVehicle().getRegNumber() :
                    (t.getExternalVehicle() != null) ? t.getExternalVehicle().getRegNumber() : "N/A";
            LocalDate itemDate = (t.getUnloadingDate() != null) ? t.getUnloadingDate() : t.getLoadingDate();
            BigDecimal balance = t.getAgreedAmount().subtract(t.getAdvanceReceived()).subtract(t.getHeldUp());
            InvoiceItemEntity item = InvoiceItemEntity.builder()
                    .invoice(invoice)
                    .transport(t)
                    .date(itemDate)
                    .vehicleRegNo(regNo)
                    .particulars(t.getDescription())
                    .rate(t.getAgreedAmount())
                    .advance(t.getAdvanceReceived())
                    .heldUp(t.getHeldUp())
                    .balance(balance)
                    .build();
            invoiceItemRepository.save(item);
            subtotal = subtotal.add(t.getAgreedAmount());
            totalAdvance = totalAdvance.add(t.getAdvanceReceived());
            totalHeldUp = totalHeldUp.add(t.getHeldUp());
            totalBalance = totalBalance.add(balance);
        }
        invoice.setSubtotal(subtotal);
        invoice.setTotalAdvance(totalAdvance);
        invoice.setTotalHeldUp(totalHeldUp);
        invoice.setTotalBalance(totalBalance);
        invoice.setTotalAmount(totalBalance); // Can be adjusted if needed
        invoice = invoiceRepository.save(invoice);
        // Update transports
        for (TransportEntity t : transports) {
            t.setInvoiceId(invoice.getId());
            t.setInvoiceStatus("Invoiced");
            transportRepository.save(t);
        }
        return toDTO(invoice);
    }
    // Get invoice by ID
    public InvoiceDTO getInvoiceById(Long id) {
        InvoiceEntity invoice = invoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        InvoiceDTO dto = toDTO(invoice);
        List<InvoiceItemEntity> items = invoiceItemRepository.findByInvoiceId(id);
        dto.setItems(items.stream().map(this::toItemDTO).collect(Collectors.toList()));
        return dto;
    }
    // Generate PDF
    public ByteArrayInputStream generateInvoicePdf(Long invoiceId) {
        InvoiceEntity invoice = invoiceRepository.findByIdAndIsDeletedFalse(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        List<InvoiceItemEntity> items = invoiceItemRepository.findByInvoiceId(invoiceId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(out));
             Document doc = new Document(pdfDoc)) {
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            // Header
            Paragraph header = new Paragraph("Kasthuri Enterprises")
                    .setFontSize(18)
                    .setFont(boldFont)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(header);
            doc.add(new Paragraph("Address: Some Address, Sri Lanka")
                    .setFont(regularFont)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Tel: 123456789 | Email: info@kasthuri.com")
                    .setFont(regularFont)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("B.R. NO: EHE/DS/ADM/07/02329")
                    .setFont(regularFont)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n").setFont(regularFont));
            // Invoice details
            Table infoTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
            infoTable.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph("Invoice No: " + invoice.getInvoiceNo()).setFont(regularFont)));
            infoTable.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph("Date: " + invoice.getGenerationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setFont(regularFont))
                    .setTextAlignment(TextAlignment.RIGHT));
            infoTable.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph("To: M/S " + invoice.getClientName()).setFont(regularFont)));
            infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)); // Empty for alignment
            doc.add(infoTable);
            doc.add(new Paragraph("\n").setFont(regularFont));
            // Items table
            float[] columnWidths = {10, 15, 30, 15, 15, 15, 15}; // Date, Vehicle No., Particulars, Rate, Advance, Held Up, Amount
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).setWidth(UnitValue.createPercentValue(100));
            // Header row
            String[] headers = {"Date", "Vehicle No.", "Particulars", "Rate", "Advance", "Held Up", "Amount"};
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setFont(boldFont))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }
            // Data rows
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (InvoiceItemEntity item : items) {
                table.addCell(new Cell().add(new Paragraph(item.getDate().format(dateFormatter)).setFont(regularFont)).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(item.getVehicleRegNo()).setFont(regularFont)).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(item.getParticulars() != null ? item.getParticulars() : "").setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(item.getRate().toString()).setFont(regularFont)).setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(item.getAdvance().toString()).setFont(regularFont)).setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(item.getHeldUp().toString()).setFont(regularFont)).setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(item.getBalance().toString()).setFont(regularFont)).setTextAlignment(TextAlignment.RIGHT));
            }
            // Total row
            table.addCell(new Cell(1, 3).add(new Paragraph("Totals").setFont(boldFont)).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(invoice.getSubtotal().toString()).setFont(boldFont)).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(invoice.getTotalAdvance().toString()).setFont(boldFont)).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(invoice.getTotalHeldUp().toString()).setFont(boldFont)).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(invoice.getTotalAmount().toString()).setFont(boldFont)).setTextAlignment(TextAlignment.RIGHT));
            doc.add(table);
            // Footer
            doc.add(new Paragraph("\nThank you for your business.").setFont(regularFont).setTextAlignment(TextAlignment.CENTER));
        } catch (IOException e) {
            throw new RuntimeException("Error creating font for PDF", e);
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }
    // DTO converters
    private InvoiceDTO toDTO(InvoiceEntity entity) {
        return InvoiceDTO.builder()
                .id(entity.getId())
                .invoiceNo(entity.getInvoiceNo())
                .generationDate(entity.getGenerationDate())
                .clientName(entity.getClientName())
                .subtotal(entity.getSubtotal())
                .totalAdvance(entity.getTotalAdvance())
                .totalHeldUp(entity.getTotalHeldUp())
                .totalBalance(entity.getTotalBalance())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .createdById(entity.getCreatedBy().getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    private InvoiceItemDTO toItemDTO(InvoiceItemEntity entity) {
        return InvoiceItemDTO.builder()
                .id(entity.getId())
                .invoiceId(entity.getInvoice().getId())
                .transportId(entity.getTransport().getId())
                .date(entity.getDate())
                .vehicleRegNo(entity.getVehicleRegNo())
                .particulars(entity.getParticulars())
                .rate(entity.getRate())
                .advance(entity.getAdvance())
                .heldUp(entity.getHeldUp())
                .balance(entity.getBalance())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}