package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.FuelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FuelRepository extends JpaRepository<FuelEntity, Long> {

    @Query("SELECT f FROM FuelEntity f WHERE f.isDelete = false")
    List<FuelEntity> findAllActive();

    @Query("SELECT f FROM FuelEntity f WHERE f.id = :id AND f.isDelete = false")
    Optional<FuelEntity> findByIdAndIsDeleteFalse(Long id);

    @Query("SELECT f FROM FuelEntity f WHERE f.isDelete = false AND f.vehicle.id = :vehicleId")
    List<FuelEntity> findByVehicleId(Long vehicleId);

    @Query("SELECT f FROM FuelEntity f WHERE f.isDelete = false AND f.transport.id = :tripId")
    List<FuelEntity> findByTripId(Long tripId);

    @Query("SELECT f FROM FuelEntity f WHERE f.isDelete = false " +
            "AND (:vehicleId IS NULL OR f.vehicle.id = :vehicleId) " +
            "AND (:startDate IS NULL OR f.fuelDate >= :startDate) " +
            "AND (:endDate IS NULL OR f.fuelDate <= :endDate) " +
            "ORDER BY f.fuelDate DESC, f.createdAt DESC")
    List<FuelEntity> findFiltered(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT f FROM FuelEntity f WHERE f.isDelete = false " +
            "AND f.vehicle.regNumber = :regNumber " +
            "AND (:startDate IS NULL OR f.fuelDate >= :startDate) " +
            "AND (:endDate IS NULL OR f.fuelDate <= :endDate)")
    List<FuelEntity> findByRegNumberAndDateRange(
            @Param("regNumber") String regNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(f.totalCost) FROM FuelEntity f " +
            "WHERE f.isDelete = false AND f.vehicle.id = :vehicleId " +
            "AND f.fuelDate >= :startDate AND f.fuelDate <= :endDate")
    BigDecimal getTotalFuelCostForVehicleInPeriod(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    //for dashboard
    @Query("SELECT COALESCE(SUM(f.totalCost), 0) FROM FuelEntity f WHERE f.isDelete = false AND f.fuelDate >= :start AND f.fuelDate <= :end")
    BigDecimal getTotalFuelCostForPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);
}