// New InvoiceRepository.java
package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    @Query("SELECT i FROM InvoiceEntity i WHERE i.isDeleted = false")
    List<InvoiceEntity> findAllActive();

    @Query("SELECT i FROM InvoiceEntity i WHERE i.id = :id AND i.isDeleted = false")
    Optional<InvoiceEntity> findByIdAndIsDeletedFalse(Long id);

    // For generating sequential invoice_no, e.g., find max for current year
    @Query("SELECT MAX(i.invoiceNo) FROM InvoiceEntity i WHERE i.invoiceNo LIKE CONCAT('INV-', :year, '-%')")
    String findMaxInvoiceNoForYear(String year);
}