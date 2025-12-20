// Updated TransportRepository.java (no major changes needed, but add if you want query for uninvoiced)
package com.enterprise.bms.enterprise_bms.repository;
import com.enterprise.bms.enterprise_bms.entity.TransportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface TransportRepository extends JpaRepository<TransportEntity, Long> {
    @Query("SELECT t FROM TransportEntity t WHERE t.isDeleted = false")
    List<TransportEntity> findAllActive();
    @Query("SELECT t FROM TransportEntity t WHERE t.id = :id AND t.isDeleted = false")
    Optional<TransportEntity> findByIdAndIsDeletedFalse(Long id);
    boolean existsByClientNameAndLoadingDateAndIsDeletedFalse(String clientName, LocalDate loadingDate);
    @Query("SELECT t FROM TransportEntity t WHERE t.isDeleted = false " +
            "AND (:ownVehicleId IS NULL OR t.ownVehicle.id = :ownVehicleId) " +
            "AND (:externalVehicleId IS NULL OR t.externalVehicle.id = :externalVehicleId) " +
            "AND (:startDate IS NULL OR t.loadingDate >= :startDate) " +
            "AND (:endDate IS NULL OR t.loadingDate <= :endDate) " +
            "ORDER BY t.loadingDate DESC, t.createdAt DESC")
    List<TransportEntity> findFiltered(
            @Param("ownVehicleId") Long ownVehicleId,
            @Param("externalVehicleId") Long externalVehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}