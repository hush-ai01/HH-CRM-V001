package com.highlands.highlandscrmbackend.fleet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTruckRequest(

        @NotBlank
        @Size(max = 50)
        String registrationNumber,

        @NotBlank
        @Size(max = 100)
        String make,

        @NotBlank
        @Size(max = 100)
        String model,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal capacity,

        @Size(max = 150)
        String assignedCompany,

        @Size(max = 150)
        String assignedOwner
) {
}