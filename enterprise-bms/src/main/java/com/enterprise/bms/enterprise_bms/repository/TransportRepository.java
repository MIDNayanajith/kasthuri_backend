package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.TransportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}