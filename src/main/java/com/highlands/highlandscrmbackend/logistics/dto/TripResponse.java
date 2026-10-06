package com.highlands.highlandscrmbackend.logistics.dto;

import com.highlands.highlandscrmbackend.logistics.Trip;
import com.highlands.highlandscrmbackend.logistics.TripStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TripResponse(

        UUID id,
        UUID companyId,

        String dispatchRef,
        OffsetDateTime dispatchDate,
        OffsetDateTime scheduledPickup,
        OffsetDateTime eta,

        String origin,
        String destination,
        String currentLocation,
        String trackingUrl,
        String dispatcherNotes,
        OffsetDateTime lastLocationAt,

        UUID driverId,
        UUID truckId,
        UUID trailerId,
        UUID gradeId,

        BigDecimal grossWeight,
        BigDecimal tareWeight,
        BigDecimal netWeight,
        BigDecimal moisturePct,

        TripStatus status,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static TripResponse from(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getCompany().getId(),

                trip.getDispatchRef(),
                trip.getDispatchDate(),
                trip.getScheduledPickup(),
                trip.getEta(),

                trip.getOrigin(),
                trip.getDestination(),
                trip.getCurrentLocation(),
                trip.getTrackingUrl(),
                trip.getDispatcherNotes(),
                trip.getLastLocationAt(),

                trip.getDriver().getId(),
                trip.getTruck().getId(),
                trip.getTrailer().getId(),
                trip.getGrade() != null ? trip.getGrade().getId() : null,

                trip.getGrossWeight(),
                trip.getTareWeight(),
                trip.getNetWeight(),
                trip.getMoisturePct(),

                trip.getStatus(),

                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}