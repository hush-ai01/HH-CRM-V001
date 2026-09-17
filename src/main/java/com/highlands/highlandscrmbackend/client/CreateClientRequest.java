package com.highlands.highlandscrmbackend.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateClientRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Client type is required")
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