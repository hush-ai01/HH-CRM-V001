package com.highlands.highlandscrmbackend.deal.dto;

import com.highlands.highlandscrmbackend.deal.DealStatus;
import com.highlands.highlandscrmbackend.deal.DealType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DealResponse(

        UUID id,

        UUID companyId,

        UUID clientId,

        UUID ownerUserId,

        String dealNumber,

        DealType type,

        DealStatus status,

        UUID commodityId,

        UUID gradeId,

        BigDecimal quantity,

        String unit,

        BigDecimal unitPrice,

        String currency,

        BigDecimal totalValue,

        LocalDate expectedCloseDate,

        String notes,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}