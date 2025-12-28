package com.enterprise.bms.enterprise_bms.controller;
import com.enterprise.bms.enterprise_bms.service.*;
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
    private final AttendanceService attendanceService;
    private final PaymentService paymentService;
    private final AdvanceService advanceService;
    private final ExVehicleService exVehicleService;
    private final FuelService fuelService;
    private final TireMaintenanceService tireMaintenanceService;
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
            throw new RuntimeException("Error generating transport Excel report", e);
        }
    }
    @GetMapping("/download/attendance")
    public ResponseEntity<Resource> downloadAttendanceExcel(
            @RequestParam(required = false) String recipientType,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) String month) {
        try {
            ByteArrayInputStream inputStream = attendanceService.generateAttendanceExcelReport(recipientType, recipientId, month);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_details.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Error generating attendance Excel report", e);
        }
    }
    @GetMapping("/download/payments")
    public ResponseEntity<Resource> downloadPaymentsExcel(
            @RequestParam(required = false) String recipientType,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) String month) {
        try {
            ByteArrayInputStream inputStream = paymentService.generatePaymentsExcelReport(recipientType, recipientId, month);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments_details.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Error generating payments Excel report", e);
        }
    }
    @GetMapping("/download/advances")
    public ResponseEntity<Resource> downloadAdvancesExcel(
            @RequestParam(required = false) String recipientType,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) String month) {
        try {
            ByteArrayInputStream inputStream = advanceService.generateAdvancesExcelReport(recipientType, recipientId, month);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=advances_details.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Error generating advances Excel report", e);
        }
    }
    @GetMapping("/download/ex-vehicles")
    public ResponseEntity<Resource> downloadExVehiclesExcel(
            @RequestParam(required = false) String regNumber,
            @RequestParam(required = false) String month) {
        try {
            ByteArrayInputStream inputStream = exVehicleService.generateExVehiclesExcelReport(regNumber, month);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ex_vehicles_details.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Error generating ex vehicles Excel report", e);
        }
    }

    @GetMapping("/download/fuel")
    public ResponseEntity<Resource> downloadFuelExcel(
            @RequestParam(required = false) String regNumber,
            @RequestParam(required = false) String month) {
        try {
            ByteArrayInputStream inputStream = fuelService.generateFuelExcelReport(regNumber, month);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fuel_details.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Error generating fuel Excel report", e);
        }
    }
    @GetMapping("/download/tire-maintenance")
    public ResponseEntity<Resource> downloadTireMaintenanceExcel(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String month) {
        try {
            ByteArrayInputStream inputStream = tireMaintenanceService.generateTireMaintenanceExcelReport(vehicleId, month);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tire_maintenance_details.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Error generating tire maintenance Excel report", e);
        }
    }
}