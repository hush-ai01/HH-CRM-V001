package com.highlands.highlandscrmbackend.fleet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateTrailerRequest(
        @NotNull
        UUID truckId,

        @NotBlank
        @Size(max = 50)
        String registrationNumber,

        @NotBlank
        @Size(max = 100)
        String type,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal capacity
) {
}