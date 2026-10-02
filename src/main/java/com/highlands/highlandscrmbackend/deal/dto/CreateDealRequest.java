package com.highlands.highlandscrmbackend.deal.dto;

import com.highlands.highlandscrmbackend.deal.DealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateDealRequest(

        @NotNull(message = "Client is required")
        UUID clientId,

        @NotNull(message = "Deal type is required")
        DealType type,

        @NotNull(message = "Commodity is required")
        UUID commodityId,

        @NotNull(message = "Grade is required")
        UUID gradeId,

        @NotNull(message = "Quantity is required")
        @DecimalMin(
                value = "0.0001",
                message = "Quantity must be greater than zero"
        )
        BigDecimal quantity,

        @NotBlank(message = "Unit is required")
        @Size(max = 20, message = "Unit must not exceed 20 characters")
        String unit,

        @NotNull(message = "Unit price is required")
        @DecimalMin(
                value = "0.0001",
                message = "Unit price must be greater than zero"
        )
        BigDecimal unitPrice,

        @NotBlank(message = "Currency is required")
        @Size(
                min = 3,
                max = 3,
                message = "Currency must be exactly 3 characters"
        )
        String currency,

        LocalDate expectedCloseDate,

        @Size(
                max = 5000,
                message = "Notes must not exceed 5000 characters"
        )
        String notes
) {
}