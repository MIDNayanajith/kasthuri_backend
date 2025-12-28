package com.enterprise.bms.enterprise_bms.controller;

import com.enterprise.bms.enterprise_bms.dto.FuelDTO;
import com.enterprise.bms.enterprise_bms.service.FuelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fuel")
public class FuelController {

    private final FuelService fuelService;

    @PostMapping
    public ResponseEntity<FuelDTO> createFuel(@RequestBody FuelDTO dto) {
        FuelDTO saved = fuelService.saveFuel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<FuelDTO>> getFilteredFuels(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(fuelService.getFilteredFuels(vehicleId, month));
    }

    @GetMapping("/by-regnumber")
    public ResponseEntity<List<FuelDTO>> getFuelsByRegNumberAndMonth(
            @RequestParam(required = false) String regNumber,
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(fuelService.getFuelsByRegNumberAndMonth(regNumber, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuelDTO> getFuelById(@PathVariable Long id) {
        FuelDTO fuel = fuelService.getFuelById(id);
        return ResponseEntity.ok(fuel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuelDTO> updateFuel(@PathVariable Long id,
                                              @RequestBody FuelDTO dto) {
        FuelDTO updated = fuelService.updateFuel(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFuel(@PathVariable Long id) {
        fuelService.deleteFuel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-cost")
    public ResponseEntity<BigDecimal> getTotalFuelCost(
            @RequestParam Long vehicleId,
            @RequestParam String month) {
        BigDecimal totalCost = fuelService.getTotalFuelCostForVehicle(vehicleId, month);
        return ResponseEntity.ok(totalCost);
    }
}