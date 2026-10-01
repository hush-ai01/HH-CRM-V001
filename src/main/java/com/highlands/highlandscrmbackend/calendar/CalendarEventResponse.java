package com.highlands.highlandscrmbackend.calendar;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CalendarEventResponse(

        UUID id,

        UUID companyId,

        UUID ownerUserId,

        String title,

        CalendarEventType type,

        OffsetDateTime scheduledAt,

        String notes,

        boolean completed,

        String linkedEntityType,

        UUID linkedEntityId,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {

    public static CalendarEventResponse from(
            CalendarEvent event
    ) {
        return new CalendarEventResponse(
                event.getId(),
                event.getCompany().getId(),
                event.getOwner().getId(),
                event.getTitle(),
                event.getType(),
                event.getScheduledAt(),
                event.getNotes(),
                event.isCompleted(),
                event.getLinkedEntityType(),
                event.getLinkedEntityId(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}