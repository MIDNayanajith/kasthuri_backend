package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.ExVehiclesDTO;
import com.enterprise.bms.enterprise_bms.entity.ExVehiclesEntity;
import com.enterprise.bms.enterprise_bms.repository.ExVehiclesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExVehicleService {

    private final ExVehiclesRepository exVehiclesRepository;

    // CREATE - Save new external vehicle with auto-calculation
    public ExVehiclesDTO saveExVehicle(ExVehiclesDTO dto) {
        validateExVehicleDTO(dto);

        ExVehiclesEntity entity = toEntity(dto);

        // Auto-calculate totalCost, balance, and paymentStatus
        calculateAndSetFinancialFields(entity);

        entity = exVehiclesRepository.save(entity);
        return toDTO(entity);
    }

    // READ - Get all active external vehicles
    public List<ExVehiclesDTO> getAllExVehicles() {
        return exVehiclesRepository.findAllActive()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // READ - Get vehicle by ID
    public ExVehiclesDTO getExVehicleById(Long id) {
        ExVehiclesEntity entity = exVehiclesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("External vehicle not found with ID: " + id));

        if (Boolean.TRUE.equals(entity.getIsDelete())) {
            throw new RuntimeException("External vehicle is deleted");
        }

        return toDTO(entity);
    }

    // UPDATE - Update external vehicle with auto-calculation
    public ExVehiclesDTO updateExVehicle(Long vehicleId, ExVehiclesDTO dto) {
        ExVehiclesEntity existing = exVehiclesRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("External vehicle not found with ID: " + vehicleId));

        if (Boolean.TRUE.equals(existing.getIsDelete())) {
            throw new RuntimeException("Cannot update deleted external vehicle");
        }

        // Update fields if provided
        if (dto.getRegNumber() != null && !dto.getRegNumber().trim().isEmpty()) {
            existing.setRegNumber(dto.getRegNumber().trim());
        }
        if (dto.getOwnerName() != null) {
            existing.setOwnerName(dto.getOwnerName());
        }
        if (dto.getOwnerContact() != null) {
            existing.setOwnerContact(dto.getOwnerContact());
        }
        if (dto.getHireRate() != null) {
            existing.setHireRate(dto.getHireRate());
        }
        if (dto.getVehicleUsage() != null) {
            existing.setVehicleUsage(dto.getVehicleUsage());
        }
        if (dto.getAdvance() != null) {
            existing.setAdvance(dto.getAdvance());
        }

        // Recalculate financial fields
        calculateAndSetFinancialFields(existing);

        existing = exVehiclesRepository.save(existing);
        return toDTO(existing);
    }

    // DELETE - Soft delete
    public void deleteExVehicle(Long vehicleId) {
        ExVehiclesEntity vehicle = exVehiclesRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("External vehicle not found with ID: " + vehicleId));

        if (Boolean.TRUE.equals(vehicle.getIsDelete())) {
            throw new RuntimeException("External vehicle is already deleted");
        }

        vehicle.setIsDelete(true);
        exVehiclesRepository.save(vehicle);
    }

    // Auto-calculation logic for financial fields
    private void calculateAndSetFinancialFields(ExVehiclesEntity entity) {
        // Calculate totalCost = hireRate * vehicleUsage
        BigDecimal totalCost = entity.getHireRate().multiply(entity.getVehicleUsage());
        entity.setTotalCost(totalCost);

        // Calculate balance = totalCost - advance
        BigDecimal balance = totalCost.subtract(entity.getAdvance());
        entity.setBalance(balance);

        // Determine payment status based on the logic
        Integer paymentStatus = determinePaymentStatus(totalCost, entity.getAdvance(), balance);
        entity.setPaymentStatus(paymentStatus);
    }

    private Integer determinePaymentStatus(BigDecimal totalCost, BigDecimal advance, BigDecimal balance) {
        // If balance = total_cost → 1 (Pending)
        if (balance.compareTo(totalCost) == 0) {
            return 1;
        }
        // If balance = 0 → 3 (Fully Paid)
        else if (balance.compareTo(BigDecimal.ZERO) == 0) {
            return 3;
        }
        // If 0 < advance < total_cost → 2 (Advance Paid)
        else if (advance.compareTo(BigDecimal.ZERO) > 0 && advance.compareTo(totalCost) < 0) {
            return 2;
        }

        return 1; // Default fallback to Pending
    }

    private void validateExVehicleDTO(ExVehiclesDTO dto) {
        if (dto.getRegNumber() == null || dto.getRegNumber().trim().isEmpty()) {
            throw new RuntimeException("Registration number is required!");
        }
        if (dto.getOwnerName() == null || dto.getOwnerName().trim().isEmpty()) {
            throw new RuntimeException("Owner name is required!");
        }
        if (dto.getOwnerContact() == null || dto.getOwnerContact().trim().isEmpty()) {
            throw new RuntimeException("Owner contact is required!");
        }
        if (dto.getHireRate() == null || dto.getHireRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valid hire rate is required!");
        }
        if (dto.getVehicleUsage() == null || dto.getVehicleUsage().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valid vehicle usage (km/days) is required!");
        }
    }

    // Helper: DTO → Entity
    private ExVehiclesEntity toEntity(ExVehiclesDTO dto) {
        return ExVehiclesEntity.builder()
                .regNumber(dto.getRegNumber().trim())
                .ownerName(dto.getOwnerName().trim())
                .ownerContact(dto.getOwnerContact().trim())
                .hireRate(dto.getHireRate())
                .vehicleUsage(dto.getVehicleUsage() != null ? dto.getVehicleUsage() : BigDecimal.ZERO)
                .advance(dto.getAdvance() != null ? dto.getAdvance() : BigDecimal.ZERO)
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .totalCost(dto.getTotalCost() != null ? dto.getTotalCost() : BigDecimal.ZERO)
                .paymentStatus(1) // Default to Pending, will be recalculated
                .isDelete(false)
                .build();
    }

    // Helper: Entity → DTO
    private ExVehiclesDTO toDTO(ExVehiclesEntity entity) {
        return ExVehiclesDTO.builder()
                .id(entity.getId())
                .regNumber(entity.getRegNumber())
                .ownerName(entity.getOwnerName())
                .ownerContact(entity.getOwnerContact())
                .hireRate(entity.getHireRate())
                .vehicleUsage(entity.getVehicleUsage())
                .advance(entity.getAdvance())
                .balance(entity.getBalance())
                .totalCost(entity.getTotalCost())
                .paymentStatus(entity.getPaymentStatus()) // Include paymentStatus in response
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}