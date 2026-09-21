package com.highlands.highlandscrmbackend.activity;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record CreateActivityRequest(

        @NotNull(message = "Activity type is required")
        ActivityType type,

        String outcome,

        String notes,

        String nextAction,

        OffsetDateTime nextActionAt
) {
}