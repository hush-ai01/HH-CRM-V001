package com.highlands.highlandscrmbackend.logistics.dto;

import com.highlands.highlandscrmbackend.logistics.TripStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeTripStatusRequest(

        @NotNull
        TripStatus status
) {
}