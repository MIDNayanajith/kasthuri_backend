// Updated InvoiceService.java (add update and delete methods)
package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.InvoiceDTO;
import com.enterprise.bms.enterprise_bms.dto.InvoiceItemDTO;
import com.enterprise.bms.enterprise_bms.entity.InvoiceEntity;
import com.enterprise.bms.enterprise_bms.entity.InvoiceItemEntity;
import com.enterprise.bms.enterprise_bms.entity.TransportEntity;
import com.enterprise.bms.enterprise_bms.entity.UserEntity;
import com.enterprise.bms.enterprise_bms.repository.InvoiceItemRepository;
import com.enterprise.bms.enterprise_bms.repository.InvoiceRepository;
import com.enterprise.bms.enterprise_bms.repository.TransportRepository;
import com.enterprise.bms.enterprise_bms.repository.UserRepository;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
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
    private final UserRepository userRepository;

    // Get all active invoices with items
    public List<InvoiceDTO> getAllInvoices() {
        List<InvoiceEntity> invoices = invoiceRepository.findAllActive();
        return invoices.stream().map(invoice -> {
            InvoiceDTO dto = toDTO(invoice);
            List<InvoiceItemEntity> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
            dto.setItems(items.stream().map(this::toItemDTO).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

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
        // Calculate totals first
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalAdvance = BigDecimal.ZERO;
        BigDecimal totalHeldUp = BigDecimal.ZERO;
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (TransportEntity t : transports) {
            subtotal = subtotal.add(t.getAgreedAmount());
            totalAdvance = totalAdvance.add(t.getAdvanceReceived());
            totalHeldUp = totalHeldUp.add(t.getHeldUp());
            BigDecimal balance = t.getAgreedAmount().subtract(t.getAdvanceReceived()).subtract(t.getHeldUp());
            totalBalance = totalBalance.add(balance);
        }
        // Create invoice with calculated totals
        InvoiceEntity invoice = InvoiceEntity.builder()
                .invoiceNo(invoiceNo)
                .generationDate(LocalDate.now())
                .clientName(clientName)
                .subtotal(subtotal)
                .totalAdvance(totalAdvance)
                .totalHeldUp(totalHeldUp)
                .totalBalance(totalBalance)
                .totalAmount(totalBalance)
                .status("Draft")
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
        invoice = invoiceRepository.save(invoice);
        // Create items
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
        }
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

    // New: Update invoice status
    public InvoiceDTO updateInvoiceStatus(Long id, String status) {
        InvoiceEntity invoice = invoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus(status);
        invoice = invoiceRepository.save(invoice);
        return toDTO(invoice);
    }

    // New: Soft delete invoice
    public void deleteInvoice(Long id) {
        InvoiceEntity invoice = invoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setIsDeleted(true);
        invoiceRepository.save(invoice);
        // Update associated transports
        List<InvoiceItemEntity> items = invoiceItemRepository.findByInvoiceId(id);
        for (InvoiceItemEntity item : items) {
            TransportEntity transport = item.getTransport();
            transport.setInvoiceId(null);
            transport.setInvoiceStatus("Not Invoiced");
            transportRepository.save(transport);
        }
    }

    // Generate PDF with updated structure
    public ByteArrayInputStream generateInvoicePdf(Long invoiceId) {
        InvoiceEntity invoice = invoiceRepository.findByIdAndIsDeletedFalse(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        List<InvoiceItemEntity> items = invoiceItemRepository.findByInvoiceId(invoiceId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(out));
             Document doc = new Document(pdfDoc)) {
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            // Load logo from classpath
            byte[] logoBytes = getClass().getClassLoader().getResourceAsStream("logo.jpeg").readAllBytes();
            ImageData logoData = ImageDataFactory.create(logoBytes);
            Image logo = new Image(logoData).scaleAbsolute(80, 50);
            // Create a table for logo and business name side by side with reduced gap
            Table headerTable = new Table(2);
            headerTable.setWidth(UnitValue.createPercentValue(80)); // Reduced width for closer spacing
            headerTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
            headerTable.setMarginBottom(5); // Reduced margin
            // Logo cell with minimal padding
            Cell logoCell = new Cell()
                    .add(logo)
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPaddingRight(5); // Reduced padding between logo and name
            // Business name cell with minimal padding
            Cell nameCell = new Cell()
                    .add(new Paragraph("Kasthuri Enterprises")
                            .setFont(boldFont)
                            .setFontSize(20)
                            .setFontColor(new DeviceRgb(0, 0, 139)))
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPaddingLeft(5); // Reduced padding
            headerTable.addCell(logoCell);
            headerTable.addCell(nameCell);
            doc.add(headerTable);
            // Center-aligned address, tel, email
            Paragraph contactInfo = new Paragraph("Address: No: 332, Napawala, Getaheththa\n" +
                    "Tel: 075 9084603 / 077 7065110\n" +
                    "Email: kasthurienterprices2014@gmail.com")
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            doc.add(contactInfo);
            // Right-aligned B.R. NO
            Paragraph brNo = new Paragraph("B.R. NO: EHE/DS/ADM/07/02329")
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10);
            doc.add(brNo);
            // Horizontal black line
            doc.add(new Paragraph()
                    .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1f))
                    .setMarginBottom(15));
            // Title "Invoice" centered
            doc.add(new Paragraph("Invoice")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));
            // Invoice No right-aligned
            Paragraph invoiceNoPara = new Paragraph("Invoice No: " + invoice.getInvoiceNo())
                    .setFont(regularFont)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(2);
            doc.add(invoiceNoPara);
            // Date right-aligned
            Paragraph datePara = new Paragraph("Date: " + invoice.getGenerationDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(regularFont)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(15);
            doc.add(datePara);
            // Client information without "M/S:" and without underline
            Paragraph clientPara = new Paragraph("Client: " + invoice.getClientName())
                    .setFont(regularFont)
                    .setMarginBottom(15)
                    .setTextAlignment(TextAlignment.LEFT);
            doc.add(clientPara);
            // Items table with borders on all sides
            float[] columnWidths = {10, 15, 30, 15, 15, 15, 15};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginBottom(15);
            // Header row with borders
            String[] headers = {"Date", "Vehicle No.", "Particulars", "Rate", "Held Up", "Advance", "Amount"};
            TextAlignment[] headerAlignments = {
                    TextAlignment.CENTER, TextAlignment.CENTER, TextAlignment.LEFT,
                    TextAlignment.RIGHT, TextAlignment.RIGHT, TextAlignment.RIGHT, TextAlignment.RIGHT
            };
            for (int i = 0; i < headers.length; i++) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(headers[i]).setFont(boldFont))
                        .setTextAlignment(headerAlignments[i])
                        .setBorder(new SolidBorder(1f))
                        .setPadding(5);
                table.addHeaderCell(headerCell);
            }
            // Data rows with borders
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            TextAlignment[] dataAlignments = {
                    TextAlignment.CENTER, TextAlignment.CENTER, TextAlignment.LEFT,
                    TextAlignment.RIGHT, TextAlignment.RIGHT, TextAlignment.RIGHT, TextAlignment.RIGHT
            };
            for (InvoiceItemEntity item : items) {
                Paragraph[] dataParas = {
                        new Paragraph(item.getDate().format(dateFormatter)).setFont(regularFont),
                        new Paragraph(item.getVehicleRegNo()).setFont(regularFont),
                        new Paragraph(item.getParticulars() != null ? item.getParticulars() : "").setFont(regularFont),
                        new Paragraph(item.getRate().toString()).setFont(regularFont),
                        new Paragraph(item.getHeldUp().toString()).setFont(regularFont),
                        new Paragraph(item.getAdvance().toString()).setFont(regularFont),
                        new Paragraph(item.getBalance().toString()).setFont(regularFont)
                };
                for (int i = 0; i < dataParas.length; i++) {
                    Cell dataCell = new Cell()
                            .add(dataParas[i])
                            .setTextAlignment(dataAlignments[i])
                            .setBorder(new SolidBorder(1f))
                            .setPadding(5);
                    table.addCell(dataCell);
                }
            }
            // Add only 5 empty rows instead of 20 to fit on one page
            int emptyRowsNeeded = Math.max(5 - items.size(), 0);
            for (int i = 0; i < emptyRowsNeeded; i++) {
                for (int j = 0; j < 7; j++) {
                    table.addCell(new Cell()
                            .setBorder(new SolidBorder(1f))
                            .setHeight(20)
                            .setPadding(5));
                }
            }
            doc.add(table);
            // Total Amount row
            Table totalTable = new Table(UnitValue.createPercentArray(columnWidths));
            totalTable.setWidth(UnitValue.createPercentValue(100));
            totalTable.setMarginBottom(15);
            // Total label spanning 6 columns
            Cell totalLabelCell = new Cell(1, 6)
                    .add(new Paragraph("Total Amount").setFont(boldFont))
                    .setBorder(new SolidBorder(1f))
                    .setPadding(5)
                    .setTextAlignment(TextAlignment.LEFT);
            // Total amount value
            Cell totalAmountCell = new Cell()
                    .add(new Paragraph(invoice.getTotalAmount().toString()).setFont(boldFont))
                    .setBorder(new SolidBorder(1f))
                    .setPadding(5)
                    .setTextAlignment(TextAlignment.RIGHT);
            totalTable.addCell(totalLabelCell);
            totalTable.addCell(totalAmountCell);
            doc.add(totalTable);
            // Footer note
            doc.add(new Paragraph("Please Issue Cheques In Favour of \"Kasthuri Enterprises\".")
                    .setFont(regularFont)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(10));
        } catch (IOException e) {
            throw new RuntimeException("Error creating font or loading image for PDF", e);
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