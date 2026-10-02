package com.highlands.highlandscrmbackend.commodity.dto;

import jakarta.validation.constraints.NotNull;

public record CommodityStatusUpdateRequest(

        @NotNull(message = "Active status is required")
        Boolean active
) {
}