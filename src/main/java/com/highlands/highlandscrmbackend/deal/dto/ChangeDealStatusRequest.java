package com.highlands.highlandscrmbackend.deal.dto;

import com.highlands.highlandscrmbackend.deal.DealStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeDealStatusRequest(

        @NotNull(message = "Deal status is required")
        DealStatus status

) {
}