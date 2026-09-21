package com.highlands.highlandscrmbackend.activity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID companyId,
        UUID clientId,
        UUID userId,
        ActivityType type,
        String outcome,
        String notes,
        String nextAction,
        OffsetDateTime nextActionAt,
        OffsetDateTime createdAt
) {

    public static ActivityResponse from(Activity activity) {

        return new ActivityResponse(
                activity.getId(),
                activity.getCompany().getId(),
                activity.getClient().getId(),
                activity.getUser().getId(),
                activity.getType(),
                activity.getOutcome(),
                activity.getNotes(),
                activity.getNextAction(),
                activity.getNextActionAt(),
                activity.getCreatedAt()
        );
    }
}