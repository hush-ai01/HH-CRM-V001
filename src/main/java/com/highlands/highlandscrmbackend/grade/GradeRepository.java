package com.highlands.highlandscrmbackend.grade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID> {

    Optional<Grade> findByIdAndCompanyId(
            UUID gradeId,
            UUID companyId
    );

    List<Grade> findAllByCompanyIdOrderByNameAsc(
            UUID companyId
    );

    List<Grade> findAllByCompanyIdAndCommodityIdOrderByNameAsc(
            UUID companyId,
            UUID commodityId
    );

    List<Grade> findAllByCompanyIdAndActiveOrderByNameAsc(
            UUID companyId,
            boolean active
    );

    List<Grade> findAllByCompanyIdAndCommodityIdAndActiveOrderByNameAsc(
            UUID companyId,
            UUID commodityId,
            boolean active
    );

    boolean existsByCompanyIdAndCommodityIdAndCode(
            UUID companyId,
            UUID commodityId,
            String code
    );

    boolean existsByCompanyIdAndCommodityIdAndCodeAndIdNot(
            UUID companyId,
            UUID commodityId,
            String code,
            UUID gradeId
    );
}