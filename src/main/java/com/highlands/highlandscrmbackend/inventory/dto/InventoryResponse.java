package com.highlands.highlandscrmbackend.inventory.dto;

import com.highlands.highlandscrmbackend.inventory.Inventory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID companyId,

        UUID commodityId,
        String commodityName,
        String commodityCode,

        UUID gradeId,
        String gradeName,
        String gradeCode,

        UUID supplierClientId,

        BigDecimal quantity,
        String unit,
        String location,
        String washPlant,
        String owner,
        String status,
        boolean portalSubmitted,
        String notes,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getCompany().getId(),

                inventory.getCommodity().getId(),
                inventory.getCommodity().getName(),
                inventory.getCommodity().getCode(),

                inventory.getGrade().getId(),
                inventory.getGrade().getName(),
                inventory.getGrade().getCode(),

                inventory.getSupplierClient() != null
                        ? inventory.getSupplierClient().getId()
                        : null,

                inventory.getQuantity(),
                inventory.getUnit(),
                inventory.getLocation(),
                inventory.getWashPlant(),
                inventory.getOwner(),
                inventory.getStatus(),
                inventory.isPortalSubmitted(),
                inventory.getNotes(),

                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}