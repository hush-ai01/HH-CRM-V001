package com.highlands.highlandscrmbackend.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTripRequest(

        @NotBlank
        String dispatchRef,

        @NotNull
        OffsetDateTime dispatchDate,

        @NotNull
        OffsetDateTime scheduledPickup,

        OffsetDateTime eta,

        @NotBlank
        String origin,

        @NotBlank
        String destination,

        String currentLocation,

        String trackingUrl,

        String dispatcherNotes,

        @NotNull
        UUID driverId,

        @NotNull
        UUID truckId,

        @NotNull
        UUID trailerId,

        UUID gradeId
) {
}