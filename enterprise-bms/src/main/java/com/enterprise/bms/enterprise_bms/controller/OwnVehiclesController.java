package com.enterprise.bms.enterprise_bms.controller;

import com.enterprise.bms.enterprise_bms.dto.OwnVehiclesDTO;
import com.enterprise.bms.enterprise_bms.service.OwnVehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/own-vehicles")

public class OwnVehiclesController {

    private final OwnVehicleService ownVehicleService;

    @PostMapping
    public ResponseEntity<OwnVehiclesDTO> createVehicle(@RequestBody OwnVehiclesDTO dto) {
        OwnVehiclesDTO saved = ownVehicleService.saveOwnVehicle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<OwnVehiclesDTO>> getAllVehicles() {
        return ResponseEntity.ok(ownVehicleService.getAllOwnVehicles());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OwnVehiclesDTO> updateVehicle(@PathVariable Long id,
                                                        @RequestBody OwnVehiclesDTO dto) {
        OwnVehiclesDTO updated = ownVehicleService.updateOwnVehicle(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        ownVehicleService.deleteOwnVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
