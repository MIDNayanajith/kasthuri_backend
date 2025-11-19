package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.DriversEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DriversRepository extends JpaRepository<DriversEntity,Long> {

    // Check if license number already exists
    Boolean existsByLicenseNumber(String licenseNumber);

    // Check if NIC number already exists
    Boolean existsByNicNo(String nicNo);

    // Fetch all active drivers (is_delete == false)
    @Query("SELECT d FROM DriversEntity d WHERE d.isDelete = false")
    List<DriversEntity> findAllByIsDeleteFalse();
}
