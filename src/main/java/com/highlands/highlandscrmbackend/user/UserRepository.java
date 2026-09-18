package com.highlands.highlandscrmbackend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCompanyIdAndEmail(
            UUID companyId,
            String email
    );

    Optional<User> findByIdAndCompanyId(
            UUID id,
            UUID companyId
    );

    List<User> findAllByCompanyId(
            UUID companyId
    );

    boolean existsByCompanyIdAndEmail(
            UUID companyId,
            String email
    );

    @Query("""
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
        FROM User u
        JOIN u.roles r
        JOIN r.permissions p
        WHERE u.id = :userId
          AND u.company.id = :companyId
          AND r.company.id = :companyId
          AND r.active = true
          AND u.active = true
          AND p.code = :permissionCode
        """)
    boolean hasPermission(
            @Param("userId") UUID userId,
            @Param("companyId") UUID companyId,
            @Param("permissionCode") String permissionCode
    );
}