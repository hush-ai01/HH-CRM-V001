package com.highlands.highlandscrmbackend.fleet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TruckRepository extends JpaRepository<Truck, UUID> {

    Optional<Truck> findByIdAndCompanyId(
            UUID truckId,
            UUID companyId
    );

    List<Truck> findAllByCompanyIdOrderByRegistrationNumberAsc(
            UUID companyId
    );

    boolean existsByCompanyIdAndRegistrationNumber(
            UUID companyId,
            String registrationNumber
    );

    boolean existsByCompanyIdAndRegistrationNumberAndIdNot(
            UUID companyId,
            String registrationNumber,
            UUID truckId
    );
}