package com.enterprise.bms.enterprise_bms.repository;

import com.enterprise.bms.enterprise_bms.entity.ExVehiclesEntity;
import com.enterprise.bms.enterprise_bms.entity.OwnVehiclesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExVehiclesRepository extends JpaRepository<ExVehiclesEntity,Long> {

    @Query("SELECT E FROM ExVehiclesEntity E WHERE E.isDelete = false")
    List<ExVehiclesEntity> findAllActive();

    @Query("SELECT E FROM ExVehiclesEntity E WHERE E.regNumber = :regNumber AND E.isDelete = false")
    Optional<ExVehiclesEntity> findByRegNumberAndIsDeleteFalse(@Param("regNumber") String regNumber);

    boolean existsByRegNumber(String regNumber);


}
