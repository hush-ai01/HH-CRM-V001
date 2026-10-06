package com.highlands.highlandscrmbackend.logistics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    Optional<Trip> findByIdAndCompanyId(
            UUID tripId,
            UUID companyId
    );

    List<Trip> findAllByCompanyIdOrderByScheduledPickupAsc(
            UUID companyId
    );

    List<Trip> findAllByCompanyIdAndStatusOrderByScheduledPickupAsc(
            UUID companyId,
            TripStatus status
    );

    List<Trip> findAllByCompanyIdAndDriverIdOrderByScheduledPickupAsc(
            UUID companyId,
            UUID driverId
    );

    List<Trip> findAllByCompanyIdAndTruckIdOrderByScheduledPickupAsc(
            UUID companyId,
            UUID truckId
    );

    List<Trip> findAllByCompanyIdAndTrailerIdOrderByScheduledPickupAsc(
            UUID companyId,
            UUID trailerId
    );

    boolean existsByCompanyIdAndDispatchRef(
            UUID companyId,
            String dispatchRef
    );

    boolean existsByCompanyIdAndDispatchRefAndIdNot(
            UUID companyId,
            String dispatchRef,
            UUID tripId
    );
}