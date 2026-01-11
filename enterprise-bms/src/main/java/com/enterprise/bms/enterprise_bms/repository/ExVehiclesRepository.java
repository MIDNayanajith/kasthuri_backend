package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.ExVehiclesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExVehiclesRepository extends JpaRepository<ExVehiclesEntity,Long> {

    @Query("SELECT E FROM ExVehiclesEntity E WHERE E.isDelete = false")
    List<ExVehiclesEntity> findAllActive();

    @Query("SELECT E FROM ExVehiclesEntity E WHERE E.regNumber = :regNumber AND E.isDelete = false")
    Optional<ExVehiclesEntity> findByRegNumberAndIsDeleteFalse(@Param("regNumber") String regNumber);

    boolean existsByRegNumber(String regNumber);

    @Query("SELECT e FROM ExVehiclesEntity e WHERE e.isDelete = false " +
            "AND (:regNumber IS NULL OR LOWER(e.regNumber) LIKE LOWER(CONCAT('%', :regNumber, '%'))) " +
            "AND (:startDate IS NULL OR e.date >= :startDate) " +
            "AND (:endDate IS NULL OR e.date <= :endDate)")
    List<ExVehiclesEntity> findFiltered(@Param("regNumber") String regNumber,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    //for dashboard
    @Query("SELECT COALESCE(SUM(e.hireRate), 0) FROM ExVehiclesEntity e WHERE e.isDelete = false AND e.date >= :start AND e.date <= :end")
    BigDecimal getTotalHireCostForPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);
}