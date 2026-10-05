package com.highlands.highlandscrmbackend.fleet.dto;

import com.highlands.highlandscrmbackend.fleet.Trailer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TrailerResponse(
        UUID id,
        UUID companyId,
        UUID truckId,
        String registrationNumber,
        String type,
        BigDecimal capacity,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static TrailerResponse from(Trailer trailer) {
        return new TrailerResponse(
                trailer.getId(),
                trailer.getCompany().getId(),
                trailer.getTruck().getId(),
                trailer.getRegistrationNumber(),
                trailer.getType(),
                trailer.getCapacity(),
                trailer.getStatus(),
                trailer.getCreatedAt(),
                trailer.getUpdatedAt()
        );
    }
}