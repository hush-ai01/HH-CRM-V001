package com.highlands.highlandscrmbackend.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInventoryRequest(

        @NotNull
        UUID commodityId,

        @NotNull
        UUID gradeId,

        UUID supplierClientId,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal quantity,

        @NotBlank
        @Size(max = 20)
        String unit,

        @NotBlank
        @Size(max = 255)
        String location,

        @Size(max = 255)
        String washPlant,

        @Size(max = 255)
        String owner,

        String notes
) {
}