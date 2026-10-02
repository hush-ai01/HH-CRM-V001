package com.highlands.highlandscrmbackend.commodity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommodityResponse(
        UUID id,
        UUID companyId,
        String name,
        String code,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}