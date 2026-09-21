package com.highlands.highlandscrmbackend.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    @Query("""
            SELECT a
            FROM Activity a
            WHERE a.id = :activityId
              AND a.company.id = :companyId
              AND a.client.id = :clientId
            """)
    Optional<Activity> findByIdAndTenantAndClient(
            @Param("activityId") UUID activityId,
            @Param("companyId") UUID companyId,
            @Param("clientId") UUID clientId
    );

    @Query("""
            SELECT a
            FROM Activity a
            WHERE a.company.id = :companyId
              AND a.client.id = :clientId
            ORDER BY a.createdAt DESC
            """)
    List<Activity> findAllByTenantAndClient(
            @Param("companyId") UUID companyId,
            @Param("clientId") UUID clientId
    );
}