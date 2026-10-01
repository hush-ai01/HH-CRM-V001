package com.highlands.highlandscrmbackend.deal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {

    Optional<Deal> findByIdAndCompanyId(
            UUID dealId,
            UUID companyId
    );

    List<Deal> findAllByCompanyIdOrderByCreatedAtDesc(
            UUID companyId
    );

    List<Deal> findAllByCompanyIdAndStatusOrderByCreatedAtDesc(
            UUID companyId,
            DealStatus status
    );

    List<Deal> findAllByCompanyIdAndClientIdOrderByCreatedAtDesc(
            UUID companyId,
            UUID clientId
    );

    boolean existsByCompanyIdAndDealNumber(
            UUID companyId,
            String dealNumber
    );
}