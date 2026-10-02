package com.highlands.highlandscrmbackend.commodity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommodityRequest(

        @NotBlank(message = "Commodity name is required")
        @Size(max = 100, message = "Commodity name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Commodity code is required")
        @Size(max = 50, message = "Commodity code must not exceed 50 characters")
        String code,

        @Size(max = 500, message = "Commodity description must not exceed 500 characters")
        String description
) {
}