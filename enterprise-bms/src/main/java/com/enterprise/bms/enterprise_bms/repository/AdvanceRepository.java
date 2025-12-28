package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.AdvanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdvanceRepository extends JpaRepository<AdvanceEntity, Long> {
    @Query("SELECT a FROM AdvanceEntity a WHERE a.isDelete = false")
    List<AdvanceEntity> findAllByIsDeleteFalse();

    @Query("SELECT a FROM AdvanceEntity a WHERE a.id = :id AND a.isDelete = false")
    Optional<AdvanceEntity> findByIdAndIsDeleteFalse(@Param("id") Long id);

    // Get undeducted advances for a recipient in a period (e.g., for deduction calculation)
    @Query("SELECT a FROM AdvanceEntity a WHERE a.isDelete = false " +
            "AND a.recipientType = :recipientType " +
            "AND a.recipientId = :recipientId " +
            "AND a.status IN ('Pending', 'Partial') " +
            "AND a.advanceDate >= :startDate AND a.advanceDate <= :endDate")
    List<AdvanceEntity> findPendingByRecipientAndPeriod(
            @Param("recipientType") String recipientType,
            @Param("recipientId") Long recipientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM AdvanceEntity a WHERE a.isDelete = false " +
            "AND (:recipientType IS NULL OR a.recipientType = :recipientType) " +
            "AND (:recipientId IS NULL OR a.recipientId = :recipientId) " +
            "AND (:startDate IS NULL OR a.advanceDate >= :startDate) " +
            "AND (:endDate IS NULL OR a.advanceDate <= :endDate) " +
            "ORDER BY a.advanceDate DESC")
    List<AdvanceEntity> findFiltered(
            @Param("recipientType") String recipientType,
            @Param("recipientId") Long recipientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM AdvanceEntity a WHERE a.deductedInPaymentId = :paymentId AND a.isDelete = false")
    List<AdvanceEntity> findByDeductedInPaymentId(@Param("paymentId") Long paymentId);
}