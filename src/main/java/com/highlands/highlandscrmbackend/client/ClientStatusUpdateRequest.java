package com.highlands.highlandscrmbackend.client;

import jakarta.validation.constraints.NotNull;

public record ClientStatusUpdateRequest(

        @NotNull(message = "Account status is required")
        AccountStatus accountStatus
) {
}