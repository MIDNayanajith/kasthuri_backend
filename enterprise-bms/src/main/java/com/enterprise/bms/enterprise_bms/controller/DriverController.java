package com.enterprise.bms.enterprise_bms.controller;


import com.enterprise.bms.enterprise_bms.dto.DriversDTO;
import com.enterprise.bms.enterprise_bms.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriversDTO> saveDriver(@RequestBody DriversDTO driversDTO){
        DriversDTO savedDriver = driverService.saveDriver(driversDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDriver);
    }

    @GetMapping
    public ResponseEntity<List<DriversDTO>> getDrivers() {
        List<DriversDTO> drivers = driverService.getDrivers();
        return ResponseEntity.ok(drivers);
    }

    @PutMapping("/{driverId}")
    public ResponseEntity<DriversDTO> updateDriver(@PathVariable Long driverId, @RequestBody DriversDTO driversDTO) {
        DriversDTO updatedDriver = driverService.updateDriver(driverId, driversDTO);
        return ResponseEntity.ok(updatedDriver);
    }

    @DeleteMapping("/{driverId}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long driverId) {
        driverService.deleteDriver(driverId);
        return ResponseEntity.noContent().build();
    }
}
