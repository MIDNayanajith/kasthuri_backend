package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.MaintenanceDTO;
import com.enterprise.bms.enterprise_bms.entity.MaintenanceEntity;
import com.enterprise.bms.enterprise_bms.entity.OwnVehiclesEntity;
import com.enterprise.bms.enterprise_bms.repository.MaintenanceRepository;
import com.enterprise.bms.enterprise_bms.repository.OwnVehiclesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceService {


    private final MaintenanceRepository maintenanceRepository;
    private final OwnVehiclesRepository ownVehiclesRepository;

    // CREATE
    @Transactional
    public MaintenanceDTO createMaintenance(MaintenanceDTO dto) {
        OwnVehiclesEntity vehicle = validateVehicle(dto.getVehicleId());

        MaintenanceEntity entity = new MaintenanceEntity();
        entity.setOwnVehicle(vehicle);
        entity.setDate(dto.getDate());
        entity.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        entity.setMileage(dto.getMileage());
        entity.setQuantity(dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1);
        entity.setUnitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO);

        calculateTotalPrice(entity);

        entity = maintenanceRepository.save(entity);
        return toDTO(entity);
    }

    // GET ALL (New Feature)
    public List<MaintenanceDTO> getAllMaintenanceRecords() {
        return maintenanceRepository.findAllActive()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET BY VEHICLE ID
    public List<MaintenanceDTO> getMaintenanceByVehicleId(Long vehicleId) {
        validateVehicle(vehicleId);
        return maintenanceRepository.findByVehicleIdAndActive(vehicleId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public MaintenanceDTO getMaintenanceById(Long id) {
        MaintenanceEntity entity = maintenanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found or deleted"));
        return toDTO(entity);
    }

    // UPDATE
    @Transactional
    public MaintenanceDTO updateMaintenance(Long id, MaintenanceDTO dto) {
        MaintenanceEntity existing = maintenanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found"));

        // ONLY validate and change vehicle if vehicleId is actually provided in DTO
        if (dto.getVehicleId() != null) {
            if (!existing.getOwnVehicle().getId().equals(dto.getVehicleId())) {
                OwnVehiclesEntity newVehicle = ownVehiclesRepository.findById(dto.getVehicleId())
                        .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + dto.getVehicleId()));
                if (Boolean.TRUE.equals(newVehicle.getIsDelete())) {
                    throw new RuntimeException("Cannot assign deleted vehicle");
                }
                existing.setOwnVehicle(newVehicle);
            }
        }
        // Remove this line completely or wrap it in null check
        // validateVehicle(dto.getVehicleId());   ← DELETE THIS LINE

        // Rest of the updates (safe - only if not null)
        if (dto.getDate() != null) existing.setDate(dto.getDate());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription().trim());
        if (dto.getMileage() != null) existing.setMileage(dto.getMileage());
        if (dto.getQuantity() != null && dto.getQuantity() > 0) existing.setQuantity(dto.getQuantity());
        if (dto.getUnitPrice() != null) existing.setUnitPrice(dto.getUnitPrice());

        calculateTotalPrice(existing);
        existing = maintenanceRepository.save(existing);
        return toDTO(existing);
    }

    // DELETE (Soft)
    @Transactional
    public void deleteMaintenance(Long id) {
        MaintenanceEntity entity = maintenanceRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new RuntimeException("Record not found or already deleted"));
        entity.setIsDelete(true);
        maintenanceRepository.save(entity);
    }

    // Helper Methods
    private OwnVehiclesEntity validateVehicle(Long vehicleId) {
        OwnVehiclesEntity vehicle = ownVehiclesRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + vehicleId));
        if (Boolean.TRUE.equals(vehicle.getIsDelete())) {
            throw new RuntimeException("Cannot add/update maintenance for deleted vehicle");
        }
        return vehicle;
    }

    private void calculateTotalPrice(MaintenanceEntity entity) {
        int qty = entity.getQuantity() != null ? entity.getQuantity() : 1;
        BigDecimal price = entity.getUnitPrice() != null ? entity.getUnitPrice() : BigDecimal.ZERO;
        entity.setTotalPrice(price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP));
    }

    private MaintenanceDTO toDTO(MaintenanceEntity e) {
        return MaintenanceDTO.builder()
                .id(e.getId())
                .vehicleId(e.getOwnVehicle().getId())
                .date(e.getDate())
                .description(e.getDescription())
                .mileage(e.getMileage())
                .quantity(e.getQuantity())
                .unitPrice(e.getUnitPrice())
                .totalPrice(e.getTotalPrice())
                .isDelete(e.getIsDelete())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

}
