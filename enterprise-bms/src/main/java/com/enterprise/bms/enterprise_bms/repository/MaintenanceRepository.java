package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.MaintenanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepository extends JpaRepository<MaintenanceEntity,Long> {

    // Get all active maintenance records (not deleted)
    @Query("SELECT m FROM MaintenanceEntity m WHERE m.isDelete = false ORDER BY m.date DESC, m.createdAt DESC")
    List<MaintenanceEntity> findAllActive();

    // Get all active records for a specific vehicle
    @Query("SELECT m FROM MaintenanceEntity m WHERE m.ownVehicle.id = :vehicleId AND m.isDelete = false")
    List<MaintenanceEntity> findByVehicleIdAndActive(@Param("vehicleId") Long vehicleId);

    // Find by ID and not deleted
    Optional<MaintenanceEntity> findByIdAndIsDeleteFalse(Long id);


}
