package com.highlands.highlandscrmbackend.commodity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommodityRepository extends JpaRepository<Commodity, UUID> {

    Optional<Commodity> findByIdAndCompanyId(
            UUID commodityId,
            UUID companyId
    );

    List<Commodity> findAllByCompanyIdOrderByNameAsc(
            UUID companyId
    );

    List<Commodity> findAllByCompanyIdAndActiveOrderByNameAsc(
            UUID companyId,
            boolean active
    );

    boolean existsByCompanyIdAndCode(
            UUID companyId,
            String code
    );

    boolean existsByCompanyIdAndCodeAndIdNot(
            UUID companyId,
            String code,
            UUID commodityId
    );
}