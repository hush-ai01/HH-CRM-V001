package com.highlands.highlandscrmbackend.calendar;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateCalendarEventRequest(

        String title,

        CalendarEventType type,

        OffsetDateTime scheduledAt,

        String notes,

        Boolean completed,

        String linkedEntityType,

        UUID linkedEntityId
) {
}