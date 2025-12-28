package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.TireMaintenanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TireMaintenanceRepository extends JpaRepository<TireMaintenanceEntity, Long> {

    // Get all active tire maintenance records (not deleted)
    @Query("SELECT t FROM TireMaintenanceEntity t WHERE t.isDelete = false ORDER BY t.date DESC, t.createdAt DESC")
    List<TireMaintenanceEntity> findAllActive();

    // Get all active records for a specific vehicle
    @Query("SELECT t FROM TireMaintenanceEntity t WHERE t.ownVehicle.id = :vehicleId AND t.isDelete = false")
    List<TireMaintenanceEntity> findByVehicleIdAndActive(@Param("vehicleId") Long vehicleId);

    // Find by ID and not deleted
    Optional<TireMaintenanceEntity> findByIdAndIsDeleteFalse(Long id);

    // Filtered query
    @Query("SELECT t FROM TireMaintenanceEntity t WHERE t.isDelete = false " +
            "AND (:vehicleId IS NULL OR t.ownVehicle.id = :vehicleId) " +
            "AND (:startDate IS NULL OR t.date >= :startDate) " +
            "AND (:endDate IS NULL OR t.date <= :endDate) " +
            "ORDER BY t.date DESC, t.createdAt DESC")
    List<TireMaintenanceEntity> findFiltered(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}