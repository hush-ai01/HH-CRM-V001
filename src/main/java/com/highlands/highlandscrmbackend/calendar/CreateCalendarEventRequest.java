package com.highlands.highlandscrmbackend.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateCalendarEventRequest(

        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Event type is required")
        CalendarEventType type,

        @NotNull(message = "Scheduled date and time are required")
        OffsetDateTime scheduledAt,

        String notes,

        String linkedEntityType,

        UUID linkedEntityId
) {
}