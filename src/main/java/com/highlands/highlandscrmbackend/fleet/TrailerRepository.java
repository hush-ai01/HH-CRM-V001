package com.highlands.highlandscrmbackend.fleet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrailerRepository extends JpaRepository<Trailer, UUID> {

    Optional<Trailer> findByIdAndCompanyId(
            UUID trailerId,
            UUID companyId
    );

    List<Trailer> findAllByCompanyIdOrderByRegistrationNumberAsc(
            UUID companyId
    );

    List<Trailer> findAllByCompanyIdAndTruckIdOrderByRegistrationNumberAsc(
            UUID companyId,
            UUID truckId
    );

    boolean existsByCompanyIdAndRegistrationNumber(
            UUID companyId,
            String registrationNumber
    );

    boolean existsByCompanyIdAndRegistrationNumberAndIdNot(
            UUID companyId,
            String registrationNumber,
            UUID trailerId
    );
}