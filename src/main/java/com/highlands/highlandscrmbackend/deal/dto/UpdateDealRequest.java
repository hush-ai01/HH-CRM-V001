package com.highlands.highlandscrmbackend.deal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateDealRequest(

        @NotNull(message = "Commodity is required")
        UUID commodityId,

        @NotNull(message = "Grade is required")
        UUID gradeId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @NotNull(message = "Unit is required")
        @Size(max = 20, message = "Unit must not exceed 20 characters")
        String unit,

        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be greater than zero")
        BigDecimal unitPrice,

        @NotNull(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
        String currency,

        LocalDate expectedCloseDate,

        @Size(max = 5000, message = "Notes must not exceed 5000 characters")
        String notes
) {
}