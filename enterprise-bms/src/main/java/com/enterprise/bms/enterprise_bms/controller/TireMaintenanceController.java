package com.enterprise.bms.enterprise_bms.controller;

import com.enterprise.bms.enterprise_bms.dto.TireMaintenanceDTO;
import com.enterprise.bms.enterprise_bms.service.TireMaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tire-maintenance")
public class TireMaintenanceController {

    private final TireMaintenanceService tireMaintenanceService;

    // Get filtered tire maintenance records
    @GetMapping
    public ResponseEntity<List<TireMaintenanceDTO>> getFilteredTireMaintenance(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String month) {
        List<TireMaintenanceDTO> records = tireMaintenanceService.getFilteredTireMaintenanceRecords(vehicleId, month);
        return ResponseEntity.ok(records);
    }

    // Get by Vehicle ID (legacy, redirects to filtered)
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<TireMaintenanceDTO>> getByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(tireMaintenanceService.getFilteredTireMaintenanceRecords(vehicleId, null));
    }

    // Get by Tire Maintenance ID
    @GetMapping("/{id}")
    public ResponseEntity<TireMaintenanceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tireMaintenanceService.getTireMaintenanceById(id));
    }

    // Create New
    @PostMapping
    public ResponseEntity<TireMaintenanceDTO> create(@RequestBody TireMaintenanceDTO dto) {
        TireMaintenanceDTO created = tireMaintenanceService.createTireMaintenance(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<TireMaintenanceDTO> update(@PathVariable Long id, @RequestBody TireMaintenanceDTO dto) {
        TireMaintenanceDTO updated = tireMaintenanceService.updateTireMaintenance(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Delete (Soft)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tireMaintenanceService.deleteTireMaintenance(id);
        return ResponseEntity.noContent().build();
    }
}