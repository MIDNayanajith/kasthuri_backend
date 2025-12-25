package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    @Query("SELECT a FROM AttendanceEntity a WHERE a.isDelete = false")
    List<AttendanceEntity> findAllByIsDeleteFalse();

    @Query("SELECT a FROM AttendanceEntity a WHERE a.id = :id AND a.isDelete = false")
    Optional<AttendanceEntity> findByIdAndIsDeleteFalse(@Param("id") Long id);

    Boolean existsByRecipientTypeAndRecipientIdAndAttendanceDate(String recipientType, Long recipientId, LocalDate attendanceDate);

    @Query("SELECT a FROM AttendanceEntity a WHERE a.isDelete = false " +
            "AND (:recipientType IS NULL OR a.recipientType = :recipientType) " +
            "AND (:recipientId IS NULL OR a.recipientId = :recipientId) " +
            "AND (:startDate IS NULL OR a.attendanceDate >= :startDate) " +
            "AND (:endDate IS NULL OR a.attendanceDate <= :endDate) " +
            "ORDER BY a.attendanceDate DESC")
    List<AttendanceEntity> findFiltered(
            @Param("recipientType") String recipientType,
            @Param("recipientId") Long recipientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}