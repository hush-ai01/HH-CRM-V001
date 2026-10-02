package com.highlands.highlandscrmbackend.grade.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GradeResponse(

        UUID id,

        UUID companyId,

        UUID commodityId,

        String commodityName,

        String commodityCode,

        String name,

        String code,

        String description,

        boolean active,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}