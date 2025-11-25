package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.TransportDTO;
import com.enterprise.bms.enterprise_bms.entity.DriversEntity;
import com.enterprise.bms.enterprise_bms.entity.ExVehiclesEntity;
import com.enterprise.bms.enterprise_bms.entity.OwnVehiclesEntity;
import com.enterprise.bms.enterprise_bms.entity.TransportEntity;
import com.enterprise.bms.enterprise_bms.repository.DriversRepository;
import com.enterprise.bms.enterprise_bms.repository.ExVehiclesRepository;
import com.enterprise.bms.enterprise_bms.repository.OwnVehiclesRepository;
import com.enterprise.bms.enterprise_bms.repository.TransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportService {

    private final TransportRepository transportRepository;
    private final OwnVehiclesRepository ownVehiclesRepository;
    private final ExVehiclesRepository exVehiclesRepository;
    private final DriversRepository driversRepository;

    // CREATE - Save new transport with auto-calculation
    public TransportDTO saveTransport(TransportDTO dto) {
        validateTransportDTO(dto);

        TransportEntity entity = toEntity(dto);

        // Auto-calculate heldUp and paymentStatus
        calculateAndSetFinancialFields(entity);

        entity = transportRepository.save(entity);
        return toDTO(entity);
    }

    // READ - Get all active transports
    public List<TransportDTO> getAllTransports() {
        return transportRepository.findAllActive()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // READ - Get transport by ID
    public TransportDTO getTransportById(Long id) {
        TransportEntity entity = transportRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Transport not found with ID: " + id));
        return toDTO(entity);
    }

    // UPDATE - Update transport with auto-calculation
    public TransportDTO updateTransport(Long transportId, TransportDTO dto) {
        TransportEntity existing = transportRepository.findByIdAndIsDeletedFalse(transportId)
                .orElseThrow(() -> new RuntimeException("Transport not found with ID: " + transportId));

        // Update fields if provided
        if (dto.getClientName() != null && !dto.getClientName().trim().isEmpty()) {
            existing.setClientName(dto.getClientName().trim());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getStartingPoint() != null) {
            existing.setStartingPoint(dto.getStartingPoint());
        }
        if (dto.getDestination() != null) {
            existing.setDestination(dto.getDestination());
        }
        if (dto.getLoadingDate() != null) {
            existing.setLoadingDate(dto.getLoadingDate());
        }
        if (dto.getUnloadingDate() != null) {
            existing.setUnloadingDate(dto.getUnloadingDate());
        }
        if (dto.getAgreedAmount() != null) {
            existing.setAgreedAmount(dto.getAgreedAmount());
        }
        if (dto.getAdvanceReceived() != null) {
            existing.setAdvanceReceived(dto.getAdvanceReceived());
        }
        if (dto.getBalanceReceived() != null) {
            existing.setBalanceReceived(dto.getBalanceReceived());
        }
        if (dto.getDistanceKm() != null) {
            existing.setDistanceKm(dto.getDistanceKm());
        }
        if (dto.getTripStatus() != null) {
            existing.setTripStatus(dto.getTripStatus());
        }

        // Update relationships if provided
        if (dto.getOwnVehicleId() != null) {
            OwnVehiclesEntity ownVehicle = ownVehiclesRepository.findById(dto.getOwnVehicleId())
                    .orElseThrow(() -> new RuntimeException("Own vehicle not found with ID: " + dto.getOwnVehicleId()));
            existing.setOwnVehicle(ownVehicle);
        }
        if (dto.getExternalVehicleId() != null) {
            ExVehiclesEntity externalVehicle = exVehiclesRepository.findById(dto.getExternalVehicleId())
                    .orElseThrow(() -> new RuntimeException("External vehicle not found with ID: " + dto.getExternalVehicleId()));
            existing.setExternalVehicle(externalVehicle);
        }
        if (dto.getInternalDriverId() != null) {
            DriversEntity driver = driversRepository.findById(dto.getInternalDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + dto.getInternalDriverId()));
            existing.setInternalDriver(driver);
        }

        // Recalculate financial fields
        calculateAndSetFinancialFields(existing);

        existing = transportRepository.save(existing);
        return toDTO(existing);
    }

    // DELETE - Soft delete
    public void deleteTransport(Long transportId) {
        TransportEntity transport = transportRepository.findByIdAndIsDeletedFalse(transportId)
                .orElseThrow(() -> new RuntimeException("Transport not found with ID: " + transportId));

        transport.setIsDeleted(true);
        transportRepository.save(transport);
    }

    // Auto-calculation logic for financial fields
    private void calculateAndSetFinancialFields(TransportEntity entity) {
        // Calculate heldUp = agreedAmount - advanceReceived - balanceReceived
        BigDecimal totalReceived = entity.getAdvanceReceived().add(entity.getBalanceReceived());
        BigDecimal heldUp = entity.getAgreedAmount().subtract(totalReceived);

        // Ensure heldUp is not negative (validate payment amounts)
        if (heldUp.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Total payments received cannot exceed agreed amount");
        }

        entity.setHeldUp(heldUp);

        // Determine payment status based on the logic
        Integer paymentStatus = determinePaymentStatus(entity.getAgreedAmount(), heldUp);
        entity.setPaymentStatus(paymentStatus);
    }

    private Integer determinePaymentStatus(BigDecimal agreedAmount, BigDecimal heldUp) {
        // If heldUp equals the full agreed_amount → 1 (Pending)
        if (heldUp.compareTo(agreedAmount) == 0) {
            return 1;
        }
        // If heldUp equals 0 → 3 (Fully Paid)
        else if (heldUp.compareTo(BigDecimal.ZERO) == 0) {
            return 3;
        }
        // If heldUp > 0 and heldUp < agreed_amount → 2 (Advance Paid)
        else if (heldUp.compareTo(BigDecimal.ZERO) > 0 && heldUp.compareTo(agreedAmount) < 0) {
            return 2;
        }

        return 1; // Default fallback to Pending
    }

    private void validateTransportDTO(TransportDTO dto) {
        if (dto.getClientName() == null || dto.getClientName().trim().isEmpty()) {
            throw new RuntimeException("Client name is required!");
        }
        if (dto.getStartingPoint() == null || dto.getStartingPoint().trim().isEmpty()) {
            throw new RuntimeException("Starting point is required!");
        }
        if (dto.getDestination() == null || dto.getDestination().trim().isEmpty()) {
            throw new RuntimeException("Destination is required!");
        }
        if (dto.getLoadingDate() == null) {
            throw new RuntimeException("Loading date is required!");
        }
        if (dto.getAgreedAmount() == null || dto.getAgreedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valid agreed amount is required!");
        }
        if (dto.getDistanceKm() == null || dto.getDistanceKm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valid distance is required!");
        }

        // Validate that at least one vehicle is assigned
        if (dto.getOwnVehicleId() == null && dto.getExternalVehicleId() == null) {
            throw new RuntimeException("Either own vehicle or external vehicle must be assigned!");
        }

        // Validate that advanceReceived and balanceReceived are not negative
        if (dto.getAdvanceReceived() != null && dto.getAdvanceReceived().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Advance received cannot be negative!");
        }
        if (dto.getBalanceReceived() != null && dto.getBalanceReceived().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Balance received cannot be negative!");
        }
    }

    // Helper: DTO → Entity
    private TransportEntity toEntity(TransportDTO dto) {
        TransportEntity.TransportEntityBuilder builder = TransportEntity.builder()
                .clientName(dto.getClientName().trim())
                .description(dto.getDescription())
                .startingPoint(dto.getStartingPoint())
                .destination(dto.getDestination())
                .loadingDate(dto.getLoadingDate())
                .unloadingDate(dto.getUnloadingDate())
                .distanceKm(dto.getDistanceKm())
                .agreedAmount(dto.getAgreedAmount())
                .advanceReceived(dto.getAdvanceReceived() != null ? dto.getAdvanceReceived() : BigDecimal.ZERO)
                .balanceReceived(dto.getBalanceReceived() != null ? dto.getBalanceReceived() : BigDecimal.ZERO)
                .tripStatus(dto.getTripStatus() != null ? dto.getTripStatus() : 1)
                .isDeleted(false);

        // Set relationships if IDs are provided
        if (dto.getOwnVehicleId() != null) {
            OwnVehiclesEntity ownVehicle = ownVehiclesRepository.findById(dto.getOwnVehicleId())
                    .orElseThrow(() -> new RuntimeException("Own vehicle not found with ID: " + dto.getOwnVehicleId()));
            builder.ownVehicle(ownVehicle);
        }
        if (dto.getExternalVehicleId() != null) {
            ExVehiclesEntity externalVehicle = exVehiclesRepository.findById(dto.getExternalVehicleId())
                    .orElseThrow(() -> new RuntimeException("External vehicle not found with ID: " + dto.getExternalVehicleId()));
            builder.externalVehicle(externalVehicle);
        }
        if (dto.getInternalDriverId() != null) {
            DriversEntity driver = driversRepository.findById(dto.getInternalDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + dto.getInternalDriverId()));
            builder.internalDriver(driver);
        }

        return builder.build();
    }

    // Helper: Entity → DTO
    private TransportDTO toDTO(TransportEntity entity) {
        return TransportDTO.builder()
                .id(entity.getId())
                .clientName(entity.getClientName())
                .description(entity.getDescription())
                .startingPoint(entity.getStartingPoint())
                .destination(entity.getDestination())
                .loadingDate(entity.getLoadingDate())
                .unloadingDate(entity.getUnloadingDate())
                .ownVehicleId(entity.getOwnVehicle() != null ? entity.getOwnVehicle().getId() : null)
                .externalVehicleId(entity.getExternalVehicle() != null ? entity.getExternalVehicle().getId() : null)
                .internalDriverId(entity.getInternalDriver() != null ? entity.getInternalDriver().getId() : null)
                .distanceKm(entity.getDistanceKm())
                .agreedAmount(entity.getAgreedAmount())
                .advanceReceived(entity.getAdvanceReceived())
                .balanceReceived(entity.getBalanceReceived())
                .heldUp(entity.getHeldUp())
                .paymentStatus(entity.getPaymentStatus())
                .tripStatus(entity.getTripStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}