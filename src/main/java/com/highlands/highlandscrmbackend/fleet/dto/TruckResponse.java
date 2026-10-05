package com.highlands.highlandscrmbackend.fleet.dto;

import com.highlands.highlandscrmbackend.fleet.Truck;
import com.highlands.highlandscrmbackend.fleet.TruckStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TruckResponse(
        UUID id,
        UUID companyId,
        String registrationNumber,
        String make,
        String model,
        BigDecimal capacity,
        TruckStatus status,
        String assignedCompany,
        String assignedOwner,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static TruckResponse from(Truck truck) {
        return new TruckResponse(
                truck.getId(),
                truck.getCompany().getId(),
                truck.getRegistrationNumber(),
                truck.getMake(),
                truck.getModel(),
                truck.getCapacity(),
                truck.getStatus(),
                truck.getAssignedCompany(),
                truck.getAssignedOwner(),
                truck.getCreatedAt(),
                truck.getUpdatedAt()
        );
    }
}