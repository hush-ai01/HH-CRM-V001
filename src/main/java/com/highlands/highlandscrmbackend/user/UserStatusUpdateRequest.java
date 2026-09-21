package com.highlands.highlandscrmbackend.user;

import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(

        @NotNull(message = "Active status is required")
        Boolean active

) {
}