package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.OwnVehiclesDTO;
import com.enterprise.bms.enterprise_bms.entity.DriversEntity;
import com.enterprise.bms.enterprise_bms.entity.OwnVehiclesEntity;
import com.enterprise.bms.enterprise_bms.repository.DriversRepository;
import com.enterprise.bms.enterprise_bms.repository.OwnVehiclesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnVehicleService {

    private final OwnVehiclesRepository ownVehiclesRepository;
    private final DriversRepository driversRepository;

    // CREATE - Save new own vehicle
    public OwnVehiclesDTO saveOwnVehicle(OwnVehiclesDTO dto) {
        if (dto.getRegNumber() == null || dto.getRegNumber().trim().isEmpty()) {
            throw new RuntimeException("Registration number is required!");
        }

        if (ownVehiclesRepository.existsByRegNumber(dto.getRegNumber().trim())) {
            throw new RuntimeException("Vehicle with registration number '" + dto.getRegNumber() + "' already exists!");
        }

        OwnVehiclesEntity entity = toEntity(dto);
        entity = ownVehiclesRepository.save(entity);
        return toDTO(entity);
    }

    // READ - Get all active vehicles
    public List<OwnVehiclesDTO> getAllOwnVehicles() {
        return ownVehiclesRepository.findAllActive()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // UPDATE - Partial update (Safe: doesn't overwrite missing fields)
    public OwnVehiclesDTO updateOwnVehicle(Long vehicleId, OwnVehiclesDTO dto) {
        OwnVehiclesEntity existing = ownVehiclesRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + vehicleId));

        // Check registration number uniqueness if changed
        if (dto.getRegNumber() != null && !dto.getRegNumber().trim().isEmpty()
                && !existing.getRegNumber().equals(dto.getRegNumber().trim())) {
            if (ownVehiclesRepository.existsByRegNumber(dto.getRegNumber().trim())) {
                throw new RuntimeException("Registration number '" + dto.getRegNumber() + "' is already taken!");
            }
            existing.setRegNumber(dto.getRegNumber().trim());
        }

        // Update only if value is provided (non-null)
        if (dto.getType() != null) {
            existing.setType(dto.getType());
        }
        if (dto.getCapacity() != null) {
            existing.setCapacity(dto.getCapacity());
        }
        if (dto.getCurrentMileage() != null) {
            existing.setCurrentMileage(dto.getCurrentMileage());
        }
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            existing.setStatus(dto.getStatus().trim());
        }

        // Handle driver assignment safely
        if (dto.getAssignedDriverId() != null) {
            DriversEntity driver = driversRepository.findById(dto.getAssignedDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + dto.getAssignedDriverId()));

            if (Boolean.TRUE.equals(driver.getIsDelete())) {
                throw new RuntimeException("Cannot assign deleted/inactive driver (ID: " + dto.getAssignedDriverId() + ")");
            }

            existing.setAssignedDriver(driver);
        } else if (dto.getAssignedDriverId() == null) {
            // Explicitly unassign driver
            existing.setAssignedDriver(null);
        }
        // If assignedDriverId not sent → keep current driver (no change)

        existing = ownVehiclesRepository.save(existing);
        return toDTO(existing);
    }

    // DELETE - Soft delete
    public void deleteOwnVehicle(Long vehicleId) {
        OwnVehiclesEntity vehicle = ownVehiclesRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + vehicleId));

        if (Boolean.TRUE.equals(vehicle.getIsDelete())) {
            throw new RuntimeException("Vehicle is already deleted");
        }

        vehicle.setIsDelete(true);
        ownVehiclesRepository.save(vehicle);
    }

    // Helper: DTO → Entity (for create)
    private OwnVehiclesEntity toEntity(OwnVehiclesDTO dto) {
        DriversEntity driver = null;

        if (dto.getAssignedDriverId() != null) {
            driver = driversRepository.findById(dto.getAssignedDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + dto.getAssignedDriverId()));

            if (Boolean.TRUE.equals(driver.getIsDelete())) {
                throw new RuntimeException("Cannot assign a deleted driver during vehicle creation");
            }
        }

        return OwnVehiclesEntity.builder()
                .regNumber(dto.getRegNumber() != null ? dto.getRegNumber().trim() : null)
                .type(dto.getType() != null ? dto.getType() : "Container")
                .capacity(dto.getCapacity())
                .currentMileage(dto.getCurrentMileage() != null ? dto.getCurrentMileage() : BigDecimal.ZERO)
                .status(dto.getStatus() != null && !dto.getStatus().isEmpty() ? dto.getStatus() : "Available")
                .assignedDriver(driver)
                .isDelete(false)
                .build();
    }

    // Helper: Entity → DTO
    private OwnVehiclesDTO toDTO(OwnVehiclesEntity entity) {
        return OwnVehiclesDTO.builder()
                .id(entity.getId())
                .regNumber(entity.getRegNumber())
                .type(entity.getType())
                .capacity(entity.getCapacity())
                .currentMileage(entity.getCurrentMileage())
                .status(entity.getStatus())
                .assignedDriverId(entity.getAssignedDriver() != null ? entity.getAssignedDriver().getId() : null)
                .isDelete(entity.getIsDelete())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}