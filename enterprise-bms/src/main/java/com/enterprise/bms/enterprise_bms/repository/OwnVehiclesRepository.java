package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.OwnVehiclesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OwnVehiclesRepository extends JpaRepository<OwnVehiclesEntity,Long> {

    boolean existsByRegNumber(String regNumber);

    @Query("SELECT v FROM OwnVehiclesEntity v WHERE v.isDelete = false")
    List<OwnVehiclesEntity> findAllActive();

    @Query("SELECT v FROM OwnVehiclesEntity v WHERE v.assignedDriver.id = :driverId AND v.isDelete = false")

    List<OwnVehiclesEntity> findByAssignedDriverId(Long driverId);
}
