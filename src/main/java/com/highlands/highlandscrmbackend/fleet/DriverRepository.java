package com.highlands.highlandscrmbackend.fleet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByIdAndCompanyId(
            UUID driverId,
            UUID companyId
    );

    List<Driver> findAllByCompanyIdOrderByLastNameAscFirstNameAsc(
            UUID companyId
    );

    boolean existsByCompanyIdAndIdentificationTypeAndIdentificationNumber(
            UUID companyId,
            DriverIdentificationType identificationType,
            String identificationNumber
    );

    boolean existsByCompanyIdAndIdentificationTypeAndIdentificationNumberAndIdNot(
            UUID companyId,
            DriverIdentificationType identificationType,
            String identificationNumber,
            UUID driverId
    );
}