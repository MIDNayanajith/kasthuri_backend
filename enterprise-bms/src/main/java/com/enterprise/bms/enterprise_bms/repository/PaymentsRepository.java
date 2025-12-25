package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.PaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentsRepository extends JpaRepository<PaymentsEntity, Long> {

    @Query("SELECT p FROM PaymentsEntity p WHERE p.isDelete = false")
    List<PaymentsEntity> findAllByIsDeleteFalse();

    @Query("SELECT p FROM PaymentsEntity p WHERE p.id = :id AND p.isDelete = false")
    Optional<PaymentsEntity> findByIdAndIsDeleteFalse(@Param("id") Long id);

    Boolean existsByRecipientTypeAndRecipientIdAndPeriodMonthAndPeriodYear(String recipientType, Long recipientId, Integer periodMonth, Integer periodYear);

    @Query("SELECT p FROM PaymentsEntity p WHERE p.isDelete = false " +
            "AND (:recipientType IS NULL OR p.recipientType = :recipientType) " +
            "AND (:recipientId IS NULL OR p.recipientId = :recipientId) " +
            "AND (:periodMonth IS NULL OR p.periodMonth = :periodMonth) " +
            "AND (:periodYear IS NULL OR p.periodYear = :periodYear) " +
            "ORDER BY p.periodYear DESC, p.periodMonth DESC")
    List<PaymentsEntity> findFiltered(
            @Param("recipientType") String recipientType,
            @Param("recipientId") Long recipientId,
            @Param("periodMonth") Integer periodMonth,
            @Param("periodYear") Integer periodYear
    );
}