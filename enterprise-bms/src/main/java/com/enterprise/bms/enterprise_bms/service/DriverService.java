package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.DriversDTO;
import com.enterprise.bms.enterprise_bms.entity.DriversEntity;
import com.enterprise.bms.enterprise_bms.repository.DriversRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriversRepository driversRepository;

    //save driver
    public DriversDTO saveDriver(DriversDTO driversDTO) {
        if (driversRepository.existsByLicenseNumber(driversDTO.getLicenseNumber())) {
            throw new RuntimeException("Driver with this license number already exists!");
        }
        DriversEntity newDriver = toEntity(driversDTO);
        newDriver = driversRepository.save(newDriver);
        return toDTO(newDriver);
    }

    // Get all active drivers (is_delete == false)
    public List<DriversDTO> getDrivers() {
        List<DriversEntity> activeDrivers = driversRepository.findAllByIsDeleteFalse();
        return activeDrivers.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

// Update driver with uniqueness checks if fields changed
    public DriversDTO updateDriver(Long driverId, DriversDTO dto) {
        DriversEntity existingDriver = driversRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver Not Found!"));

        // Check license uniqueness if changed
        String newLicense = dto.getLicenseNumber();
        if (!existingDriver.getLicenseNumber().equals(newLicense) &&
                driversRepository.existsByLicenseNumber(newLicense)) {
            throw new RuntimeException("License number already exists!");
        }

        // Check NIC uniqueness if changed
        String newNic = dto.getNicNo();
        if (!existingDriver.getNicNo().equals(newNic) &&
                driversRepository.existsByNicNo(newNic)) {
            throw new RuntimeException("NIC number already exists!");
        }

        // Update fields
        existingDriver.setName(dto.getName());
        existingDriver.setLicenseNumber(newLicense);
        existingDriver.setNicNo(newNic);
        existingDriver.setContact(dto.getContact());
        existingDriver.setAddress(dto.getAddress());
        existingDriver.setHireDate(dto.getHireDate());
        existingDriver.setPaymentRate(dto.getPaymentRate());

        existingDriver = driversRepository.save(existingDriver);
        return toDTO(existingDriver);
    }

    // Soft delete driver by setting isDelete to true
    public void deleteDriver(Long driverId) {
        DriversEntity existingDriver = driversRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver Not Found!"));

        if (existingDriver.getIsDelete()) {
            throw new RuntimeException("Driver already deleted!");
        }

        existingDriver.setIsDelete(true);
        driversRepository.save(existingDriver);
    }

    //helper methods
    private DriversEntity toEntity(DriversDTO driversDTO){
        return DriversEntity.builder()
                .name(driversDTO.getName())
                .licenseNumber(driversDTO.getLicenseNumber())
                .nicNo(driversDTO.getNicNo())
                .contact(driversDTO.getContact())
                .address(driversDTO.getAddress())
                .hireDate(driversDTO.getHireDate())
                .paymentRate(driversDTO.getPaymentRate())
                .isDelete(false)
                .build();
    }

    private DriversDTO toDTO(DriversEntity entity){
        return DriversDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .licenseNumber(entity.getLicenseNumber())
                .nicNo(entity.getNicNo())
                .contact(entity.getContact())
                .address(entity.getAddress())
                .hireDate(entity.getHireDate())
                .paymentRate(entity.getPaymentRate())
                .isDelete(entity.getIsDelete())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
