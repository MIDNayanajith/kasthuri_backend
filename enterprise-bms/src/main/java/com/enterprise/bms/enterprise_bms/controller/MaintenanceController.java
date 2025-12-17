// Updated MaintenanceController.java
package com.enterprise.bms.enterprise_bms.controller;
import com.enterprise.bms.enterprise_bms.dto.MaintenanceDTO;
import com.enterprise.bms.enterprise_bms.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;
    // 1. Get filtered maintenance records
    @GetMapping
    public ResponseEntity<List<MaintenanceDTO>> getFilteredMaintenance(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String month) {
        List<MaintenanceDTO> records = maintenanceService.getFilteredMaintenanceRecords(vehicleId, month);
        return ResponseEntity.ok(records);
    }
    // 2. Get by Vehicle ID (legacy, but can keep or redirect to filtered)
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<MaintenanceDTO>> getByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(maintenanceService.getFilteredMaintenanceRecords(vehicleId, null));
    }
    // 3. Get by Maintenance ID
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.getMaintenanceById(id));
    }
    // 4. Create New
    @PostMapping
    public ResponseEntity<MaintenanceDTO> create(@RequestBody MaintenanceDTO dto) {
        MaintenanceDTO created = maintenanceService.createMaintenance(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    // 5. Update
    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceDTO> update(@PathVariable Long id, @RequestBody MaintenanceDTO dto) {
        MaintenanceDTO updated = maintenanceService.updateMaintenance(id, dto);
        return ResponseEntity.ok(updated);
    }
    // 6. Delete (Soft)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        maintenanceService.deleteMaintenance(id);
        return ResponseEntity.noContent().build();
    }
}