package com.enterprise.bms.enterprise_bms.controller;

import com.enterprise.bms.enterprise_bms.dto.ExVehiclesDTO;
import com.enterprise.bms.enterprise_bms.service.ExVehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ex-vehicles")
public class ExVehiclesController {
    private final ExVehicleService exVehicleService;

    @PostMapping
    public ResponseEntity<ExVehiclesDTO> createVehicle(@RequestBody ExVehiclesDTO dto) {
        ExVehiclesDTO saved = exVehicleService.saveExVehicle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ExVehiclesDTO>> getAllVehicles(
            @RequestParam(required = false) String regNumber,
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(exVehicleService.getFilteredExVehicles(regNumber, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExVehiclesDTO> getVehicleById(@PathVariable Long id) {
        ExVehiclesDTO vehicle = exVehicleService.getExVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExVehiclesDTO> updateVehicle(@PathVariable Long id,
                                                       @RequestBody ExVehiclesDTO dto) {
        ExVehiclesDTO updated = exVehicleService.updateExVehicle(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/make-payment")
    public ResponseEntity<ExVehiclesDTO> makePayment(@PathVariable Long id,
                                                     @RequestBody Map<String, BigDecimal> paymentRequest) {
        BigDecimal paymentAmount = paymentRequest.get("paymentAmount");
        if (paymentAmount == null) {
            throw new RuntimeException("Payment amount is required");
        }
        ExVehiclesDTO updated = exVehicleService.makePayment(id, paymentAmount);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        exVehicleService.deleteExVehicle(id);
        return ResponseEntity.noContent().build();
    }
}