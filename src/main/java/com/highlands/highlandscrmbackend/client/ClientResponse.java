package com.highlands.highlandscrmbackend.client;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientResponse(

        UUID id,

        UUID companyId,

        String name,

        ClientType type,

        String area,

        BigDecimal monthlyQuantity,

        BigDecimal weeklyQuantity,

        String paymentTerms,

        DeliveryTerm deliveryTerms,

        String warehouse,

        String contactName,

        String contactPhone,

        String contactEmail,

        AccountStatus accountStatus,

        String nextAction,

        OffsetDateTime nextActionAt,

        UUID ownerUserId,

        ClientVisibility visibility,

        UUID sourceLeadId,

        UUID originatorUserId,

        UUID createdByUserId,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}