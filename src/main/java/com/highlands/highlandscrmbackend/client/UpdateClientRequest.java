package com.highlands.highlandscrmbackend.client;

import jakarta.validation.constraints.Email;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateClientRequest(

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

        @Email(message = "Contact email must be valid")
        String contactEmail,

        AccountStatus accountStatus,

        String nextAction,

        OffsetDateTime nextActionAt,

        UUID ownerUserId,

        ClientVisibility visibility

) {
}