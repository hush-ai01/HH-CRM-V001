package com.highlands.highlandscrmbackend.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByIdAndCompanyId(
            UUID inventoryId,
            UUID companyId
    );

    List<Inventory> findAllByCompanyIdOrderByCreatedAtDesc(
            UUID companyId
    );

    List<Inventory> findAllByCompanyIdAndStatusOrderByCreatedAtDesc(
            UUID companyId,
            String status
    );

    List<Inventory> findAllByCompanyIdAndCommodityIdOrderByCreatedAtDesc(
            UUID companyId,
            UUID commodityId
    );

    List<Inventory> findAllByCompanyIdAndGradeIdOrderByCreatedAtDesc(
            UUID companyId,
            UUID gradeId
    );

    List<Inventory> findAllByCompanyIdAndSupplierClientIdOrderByCreatedAtDesc(
            UUID companyId,
            UUID supplierClientId
    );
}