// Updated TransportController.java - Add invoiceStatus parameter
package com.enterprise.bms.enterprise_bms.controller;
import com.enterprise.bms.enterprise_bms.dto.TransportDTO;
import com.enterprise.bms.enterprise_bms.service.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/transports")
public class TransportController {
    private final TransportService transportService;
    @PostMapping
    public ResponseEntity<TransportDTO> createTransport(@RequestBody TransportDTO dto) {
        TransportDTO saved = transportService.saveTransport(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @GetMapping
    public ResponseEntity<List<TransportDTO>> getFilteredTransports(
            @RequestParam(required = false) Long ownVehicleId,
            @RequestParam(required = false) Long externalVehicleId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String invoiceStatus) { // Add invoiceStatus parameter
        return ResponseEntity.ok(transportService.getFilteredTransports(ownVehicleId, externalVehicleId, month, invoiceStatus));
    }
    @GetMapping("/{id}")
    public ResponseEntity<TransportDTO> getTransportById(@PathVariable Long id) {
        TransportDTO transport = transportService.getTransportById(id);
        return ResponseEntity.ok(transport);
    }
    @PutMapping("/{id}")
    public ResponseEntity<TransportDTO> updateTransport(@PathVariable Long id,
                                                        @RequestBody TransportDTO dto) {
        TransportDTO updated = transportService.updateTransport(id, dto);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransport(@PathVariable Long id) {
        transportService.deleteTransport(id);
        return ResponseEntity.noContent().build();
    }
}