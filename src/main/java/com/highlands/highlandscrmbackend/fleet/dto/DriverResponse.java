package com.highlands.highlandscrmbackend.fleet.dto;

import com.highlands.highlandscrmbackend.fleet.Driver;
import com.highlands.highlandscrmbackend.fleet.DriverIdentificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DriverResponse(
        UUID id,
        UUID companyId,
        String firstName,
        String lastName,
        DriverIdentificationType identificationType,
        String identificationNumber,
        String phone,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getCompany().getId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getIdentificationType(),
                driver.getIdentificationNumber(),
                driver.getPhone(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }
}