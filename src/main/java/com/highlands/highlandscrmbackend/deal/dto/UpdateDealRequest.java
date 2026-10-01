package com.highlands.highlandscrmbackend.deal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDealRequest(

        @Size(max = 100, message = "Commodity must not exceed 100 characters")
        String commodity,

        @Size(max = 100, message = "Grade must not exceed 100 characters")
        String grade,

        @DecimalMin(
                value = "0.0001",
                message = "Quantity must be greater than zero"
        )
        BigDecimal quantity,

        @Size(max = 20, message = "Unit must not exceed 20 characters")
        String unit,

        @DecimalMin(
                value = "0.0001",
                message = "Unit price must be greater than zero"
        )
        BigDecimal unitPrice,

        @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
        String currency,

        LocalDate expectedCloseDate,

        String notes
) {
}