// Updated ExcelController.java
package com.enterprise.bms.enterprise_bms.controller;
import com.enterprise.bms.enterprise_bms.service.MaintenanceService;
import com.enterprise.bms.enterprise_bms.service.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayInputStream;
@RestController
@RequiredArgsConstructor
@RequestMapping("/excel")
public class ExcelController {
    private final MaintenanceService maintenanceService;
    private final TransportService transportService;
    @GetMapping("/download/maintenance")
    public ResponseEntity<Resource> downloadMaintenanceExcel(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String month) {
        try {
            ByteArrayInputStream inputStream = maintenanceService.generateMaintenanceExcelReport(vehicleId, month);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=maintenance_details.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            // In a production environment, log the error appropriately
            throw new RuntimeException("Error generating maintenance Excel report", e);
        }
    }
    @GetMapping("/download/transport")
    public ResponseEntity<Resource> downloadTransportExcel(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String month) {
        try {
            ByteArrayInputStream inputStream = transportService.generateTransportExcelReport(vehicleId, month);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transport_details.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            // In a production environment, log the error appropriately
            throw new RuntimeException("Error generating transport Excel report", e);
        }
    }
}